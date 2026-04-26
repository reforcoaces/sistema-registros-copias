package br.com.sistemacopias.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderRecord {
    private String id;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
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
