package br.com.sistemacopias.dto;

import java.math.BigDecimal;

public class RevenueSummary {
    private String periodLabel;
    private BigDecimal totalAmount;

    public RevenueSummary(String periodLabel, BigDecimal totalAmount) {
        this.periodLabel = periodLabel;
        this.totalAmount = totalAmount;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
