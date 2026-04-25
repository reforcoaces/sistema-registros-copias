package br.com.sistemacopias.schedule;

import br.com.sistemacopias.repository.OrderRepository;
import br.com.sistemacopias.service.MailNotificationService;
import br.com.sistemacopias.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledEmailJobs {
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final MailNotificationService mailNotificationService;

    public ScheduledEmailJobs(OrderService orderService,
                              OrderRepository orderRepository,
                              MailNotificationService mailNotificationService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.mailNotificationService = mailNotificationService;
    }

    /** Todo dia as 20:00 (horario de Brasilia): faturamento do dia. */
    @Scheduled(cron = "0 0 20 * * *", zone = "America/Sao_Paulo")
    public void sendDailyRevenueEmail() {
        LocalDate today = LocalDate.now(ZONE);
        BigDecimal total = orderService.getRevenueForDate(today);
        String valor = total.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",");
        String html = "<p>Faturamento do dia <strong>" + today.format(FMT) + "</strong> (America/Sao_Paulo).</p>"
                + "<p style=\"font-size:1.4em;color:#1e7dfa;\"><strong>R$ " + valor + "</strong></p>"
                + "<p>Sistema de registros de copias.</p>";
        mailNotificationService.sendSimple("[Copias] Faturamento do dia " + today.format(FMT), html);
    }

    /**
     * Ultimo dia do mes, as 20:05: envia backup do arquivo de pedidos.
     * Roda nos dias 28-31 e envia apenas se amanha for dia 1.
     */
    @Scheduled(cron = "0 5 20 28-31 * *", zone = "America/Sao_Paulo")
    public void sendMonthlyOrdersBackup() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate tomorrow = today.plusDays(1);
        if (tomorrow.getDayOfMonth() != 1) {
            return;
        }
        YearMonth ym = YearMonth.from(today);
        String fileName = "orders-backup-" + ym + ".json";
        String subject = "[Copias] Backup mensal " + ym;
        String html = "<p>Backup automatico do arquivo de pedidos referente a <strong>" + ym + "</strong>.</p>"
                + "<p>Anexo: <code>" + fileName + "</code></p>";
        mailNotificationService.sendWithAttachment(subject, html, orderRepository.getDataFilePath(), fileName);
    }
}
