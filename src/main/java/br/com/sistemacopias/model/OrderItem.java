package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Embeddable
public class OrderItem {
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(precision = 19, scale = 2)
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
