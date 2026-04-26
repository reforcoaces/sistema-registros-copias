package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.ImportResult;
import br.com.sistemacopias.model.OrderItem;
import br.com.sistemacopias.model.OrderRecord;
import br.com.sistemacopias.model.PaymentMethod;
import br.com.sistemacopias.model.ProductType;
import br.com.sistemacopias.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RetroactiveImportService {
    private static final Pattern DATE_DD_MM_COMPACT_YEAR = Pattern.compile("^(\\d{2})/(\\d{2})(\\d{4})$");
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DD_MM_YY = DateTimeFormatter.ofPattern("dd/MM/uu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private final OrderRepository orderRepository;

    public RetroactiveImportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ImportResult importFromCsvText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ImportResult(0, 0);
        }

        int imported = 0;
        int skipped = 0;
        String[] lines = rawText.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank() || normalize(line).startsWith("data,produto")) {
                continue;
            }

            String[] columns = line.split(",", -1);
            if (columns.length < 5) {
                skipped++;
                continue;
            }

            String dateRaw = columns[0].trim();
            String productRaw = columns[1].trim();
            String quantityRaw = columns[2].trim();
            String totalRaw = columns[3].trim();
            String paymentRaw = columns[4].trim();

            try {
                LocalDate date = parseDate(dateRaw);
                BigDecimal total = parseCurrency(totalRaw);
                PaymentMethod payment = parsePayment(paymentRaw);
                ProductType productType = inferProductType(productRaw, quantityRaw);
                int quantity = inferQuantity(quantityRaw);
                BigDecimal unitPrice = total.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);

                OrderItem item = new OrderItem(productType, quantity, unitPrice, total);
                LocalDateTime createdAt = LocalDateTime.of(date, LocalTime.of(12, 0)).plusSeconds(i);
                OrderRecord order = OrderRecord.createWithDate(List.of(item), total, payment, createdAt);
                orderRepository.save(order);
                imported++;
            } catch (Exception ignored) {
                skipped++;
            }
        }

        return new ImportResult(imported, skipped);
    }

    /**
     * Data no arquivo: dia/mes/ano (formato brasileiro {@code dd/MM/yyyy}).
     * O ano vem do proprio arquivo (2027, etc.).
     * Aceita data sem a barra antes do ano: {@code 01/072026} vira {@code 01/07/2026}.
     */
    private LocalDate parseDate(String value) {
        String cleaned = value.replace(" ", "");
        Matcher compact = DATE_DD_MM_COMPACT_YEAR.matcher(cleaned);
        if (compact.matches()) {
            cleaned = compact.group(1) + "/" + compact.group(2) + "/" + compact.group(3);
        }
        try {
            return LocalDate.parse(cleaned, DD_MM_YYYY);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(cleaned, DD_MM_YY);
        }
    }

    private BigDecimal parseCurrency(String value) {
        String cleaned = value.replace("R$", "").replace(" ", "");
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(",", ".");
        }
        if (cleaned.isBlank() || cleaned.equals("--")) {
            throw new IllegalArgumentException("Valor total invalido");
        }
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }

    private PaymentMethod parsePayment(String value) {
        String normalized = normalize(value);
        if (normalized.contains("pix")) {
            return PaymentMethod.PIX;
        }
        if (normalized.contains("debito")) {
            return PaymentMethod.DEBITO;
        }
        if (normalized.contains("credito") || normalized.contains("cartao")) {
            return PaymentMethod.CREDITO;
        }
        return PaymentMethod.DINHEIRO;
    }

    private ProductType inferProductType(String product, String quantityText) {
        String text = normalize(product + " " + quantityText);
        if (text.contains("plastif")) {
            return ProductType.PLASTIFICACAO;
        }
        if (text.contains("digit")) {
            return ProductType.DIGITALIZACAO;
        }
        if (text.contains("impress") || text.contains("curriculum")) {
            return text.contains("color") ? ProductType.IMPRESSAO_COLORIDA : ProductType.IMPRESSAO_PRETO_BRANCO;
        }
        if (text.contains("xerox") || text.contains("copia")) {
            return text.contains("color") ? ProductType.COPIA_COLORIDA : ProductType.COPIA_PRETO_BRANCO;
        }
        return ProductType.IMPRESSAO_PRETO_BRANCO;
    }

    private int inferQuantity(String quantityText) {
        String normalized = normalize(quantityText);
        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        if (matcher.find()) {
            int parsed = Integer.parseInt(matcher.group(1));
            return Math.max(parsed, 1);
        }
        return 1;
    }

    private String normalize(String value) {
        String text = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return text.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
