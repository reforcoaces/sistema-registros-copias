package br.com.sistemacopias.dto;

import java.math.BigDecimal;

public class DashboardDayBar {
    private final String label;
    private final BigDecimal amount;
    private final int barPercent;

    public DashboardDayBar(String label, BigDecimal amount, int barPercent) {
        this.label = label;
        this.amount = amount;
        this.barPercent = barPercent;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getBarPercent() {
        return barPercent;
    }
}
