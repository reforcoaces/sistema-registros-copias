package br.com.sistemacopias.model;

import java.math.BigDecimal;

public enum ProductType {
    COPIA_PRETO_BRANCO("Copia Preto e Branco", new BigDecimal("1.00")),
    COPIA_COLORIDA("Copia Colorida", new BigDecimal("2.00")),
    IMPRESSAO_PRETO_BRANCO("Impressao Preto e Branco", new BigDecimal("2.00")),
    IMPRESSAO_COLORIDA("Impressao Colorida", new BigDecimal("3.00")),
    DIGITALIZACAO("Digitalizacao", new BigDecimal("1.00")),
    PLASTIFICACAO("Plastificacao", new BigDecimal("5.00"));

    private final String descricao;
    private final BigDecimal unitPrice;

    ProductType(String descricao, BigDecimal unitPrice) {
        this.descricao = descricao;
        this.unitPrice = unitPrice;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
