package br.com.sistemacopias.model;

import java.math.BigDecimal;

public class OrderItem {
    private ProductType productType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;

    public OrderItem() {
    }

    public OrderItem(ProductType productType, int quantity, BigDecimal unitPrice, BigDecimal total) {
        this.productType = productType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
