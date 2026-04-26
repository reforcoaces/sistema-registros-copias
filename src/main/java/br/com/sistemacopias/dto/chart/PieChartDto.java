package br.com.sistemacopias.dto.chart;

import java.util.Collections;
import java.util.List;

/**
 * Dados para grafico circular (CSS conic-gradient) + legenda.
 */
public class PieChartDto {
    private final String gradientCss;
    private final List<LegendEntry> legend;

    public PieChartDto(String gradientCss, List<LegendEntry> legend) {
        this.gradientCss = gradientCss;
        this.legend = legend != null ? legend : Collections.emptyList();
    }

    public String getGradientCss() {
        return gradientCss;
    }

    public List<LegendEntry> getLegend() {
        return legend;
    }

    public static final class LegendEntry {
        private final String label;
        private final String amountLabel;
        private final String color;

        public LegendEntry(String label, String amountLabel, String color) {
            this.label = label;
            this.amountLabel = amountLabel;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public String getAmountLabel() {
            return amountLabel;
        }

        public String getColor() {
            return color;
        }
    }
}
