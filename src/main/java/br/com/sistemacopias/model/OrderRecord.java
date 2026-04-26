package br.com.sistemacopias.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "copia_pedido")
public class OrderRecord {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "copia_pedido_item",
            joinColumns = @JoinColumn(name = "pedido_id", referencedColumnName = "id", nullable = false))
    @OrderColumn(name = "line_idx")
    private List<OrderItem> items = new ArrayList<>();
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    public OrderRecord() {
    }

    public static OrderRecord createNew(List<OrderItem> items, BigDecimal totalAmount, PaymentMethod paymentMethod) {
        OrderRecord orderRecord = new OrderRecord();
        orderRecord.id = UUID.randomUUID().toString();
        orderRecord.createdAt = LocalDateTime.now();
        orderRecord.items = items;
        orderRecord.totalAmount = totalAmount;
        orderRecord.paymentMethod = paymentMethod;
        return orderRecord;
    }

    public static OrderRecord createWithDate(List<OrderItem> items,
                                             BigDecimal totalAmount,
                                             PaymentMethod paymentMethod,
                                             LocalDateTime createdAt) {
        OrderRecord orderRecord = new OrderRecord();
        orderRecord.id = UUID.randomUUID().toString();
        orderRecord.createdAt = createdAt;
        orderRecord.items = items;
        orderRecord.totalAmount = totalAmount;
        orderRecord.paymentMethod = paymentMethod;
        return orderRecord;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
