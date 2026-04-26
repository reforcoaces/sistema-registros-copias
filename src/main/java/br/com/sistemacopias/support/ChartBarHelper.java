package br.com.sistemacopias.support;

import br.com.sistemacopias.dto.chart.BarChartRowDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ChartBarHelper {
    private ChartBarHelper() {
    }

    public static List<BarChartRowDto> horizontalFromMoney(Map<String, BigDecimal> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        List<Map.Entry<String, BigDecimal>> list = data.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(Map.Entry<String, BigDecimal>::getValue).reversed())
                .toList();
        if (list.isEmpty()) {
            return List.of();
        }
        BigDecimal max = list.stream().map(Map.Entry::getValue).max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        if (max.compareTo(BigDecimal.ZERO) <= 0) {
            max = BigDecimal.ONE;
        }
        DecimalFormat moneyFmt = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));
        List<BarChartRowDto> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : list) {
            int pct = e.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(max, 0, RoundingMode.HALF_UP)
                    .intValue();
            pct = Math.max(pct, 4);
            rows.add(new BarChartRowDto(e.getKey(), pct, "R$ " + moneyFmt.format(e.getValue())));
        }
        return rows;
    }

    public static List<BarChartRowDto> horizontalFromCounts(Map<String, Integer> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        List<Map.Entry<String, Integer>> list = data.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed())
                .toList();
        if (list.isEmpty()) {
            return List.of();
        }
        int max = list.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
        List<BarChartRowDto> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> e : list) {
            int pct = (int) Math.round(100.0 * e.getValue() / max);
            pct = Math.max(pct, 6);
            rows.add(new BarChartRowDto(e.getKey(), pct, String.valueOf(e.getValue())));
        }
        return rows;
    }
}
