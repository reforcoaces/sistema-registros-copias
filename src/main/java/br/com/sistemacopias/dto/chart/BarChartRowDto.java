package br.com.sistemacopias.dto.chart;

public class BarChartRowDto {
    private final String label;
    private final int percentWidth;
    private final String valueLabel;

    public BarChartRowDto(String label, int percentWidth, String valueLabel) {
        this.label = label;
        this.percentWidth = Math.max(0, Math.min(100, percentWidth));
        this.valueLabel = valueLabel;
    }

    public String getLabel() {
        return label;
    }

    public int getPercentWidth() {
        return percentWidth;
    }

    public String getValueLabel() {
        return valueLabel;
    }
}
