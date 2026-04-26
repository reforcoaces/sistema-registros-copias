package br.com.sistemacopias.support;

import br.com.sistemacopias.dto.chart.PieChartDto;
import br.com.sistemacopias.dto.chart.PieChartDto.LegendEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ChartPieHelper {
    private static final String[] PALETTE = {
            "#2563eb", "#16a34a", "#ea580c", "#9333ea", "#0891b2",
            "#ca8a04", "#dc2626", "#64748b", "#db2777", "#4f46e5"
    };

    private ChartPieHelper() {
    }

    public static Optional<PieChartDto> buildFromMoneyMap(Map<String, BigDecimal> data) {
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        List<Map.Entry<String, BigDecimal>> positive = data.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e) -> e.getValue()).reversed())
                .toList();
        if (positive.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal total = positive.stream().map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        return Optional.of(build(positive, total, true));
    }

    public static Optional<PieChartDto> buildFromCounts(Map<String, Integer> data) {
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        List<Map.Entry<String, Integer>> positive = data.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed())
                .toList();
        if (positive.isEmpty()) {
            return Optional.empty();
        }
        int total = positive.stream().mapToInt(Map.Entry::getValue).sum();
        if (total <= 0) {
            return Optional.empty();
        }
        List<Map.Entry<String, BigDecimal>> asBd = new ArrayList<>();
        for (Map.Entry<String, Integer> e : positive) {
            asBd.add(Map.entry(e.getKey(), BigDecimal.valueOf(e.getValue())));
        }
        return Optional.of(build(asBd, BigDecimal.valueOf(total), false));
    }

    private static PieChartDto build(List<Map.Entry<String, BigDecimal>> entries, BigDecimal total, boolean moneyLegend) {
        DecimalFormat moneyFmt = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));
        StringBuilder grad = new StringBuilder("conic-gradient(");
        List<LegendEntry> legend = new ArrayList<>();
        double accDeg = 0;
        int n = entries.size();
        for (int i = 0; i < n; i++) {
            Map.Entry<String, BigDecimal> e = entries.get(i);
            BigDecimal v = e.getValue();
            double sweep = v.multiply(BigDecimal.valueOf(360))
                    .divide(total, 8, RoundingMode.HALF_UP)
                    .doubleValue();
            if (i == n - 1) {
                sweep = 360.0 - accDeg;
            }
            double start = accDeg;
            double end = accDeg + sweep;
            String color = PALETTE[i % PALETTE.length];
            if (i > 0) {
                grad.append(", ");
            }
            grad.append(String.format(Locale.US, "%s %.4fdeg %.4fdeg", color, start, end));
            int iv = v.intValue();
            String amountLabel = moneyLegend
                    ? ("R$ " + moneyFmt.format(v))
                    : (iv + (iv == 1 ? " aluno" : " alunos"));
            legend.add(new LegendEntry(e.getKey(), amountLabel, color));
            accDeg = end;
        }
        grad.append(")");
        return new PieChartDto(grad.toString(), legend);
    }

    /** Monta mapa ordenado para pizza de resumo (fluxo). */
    public static LinkedHashMap<String, BigDecimal> orderedMap(String k1, BigDecimal v1, String k2, BigDecimal v2) {
        LinkedHashMap<String, BigDecimal> m = new LinkedHashMap<>();
        m.put(k1, v1 != null ? v1 : BigDecimal.ZERO);
        m.put(k2, v2 != null ? v2 : BigDecimal.ZERO);
        return m;
    }

    public static LinkedHashMap<String, BigDecimal> orderedTriple(
            String k1, BigDecimal v1, String k2, BigDecimal v2, String k3, BigDecimal v3) {
        LinkedHashMap<String, BigDecimal> m = new LinkedHashMap<>();
        m.put(k1, v1 != null ? v1 : BigDecimal.ZERO);
        m.put(k2, v2 != null ? v2 : BigDecimal.ZERO);
        m.put(k3, v3 != null ? v3 : BigDecimal.ZERO);
        return m;
    }
}
