package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrderForm {
    @Valid
    private List<OrderItemForm> items = new ArrayList<>();

    @NotNull(message = "Selecione o meio de pagamento")
    private PaymentMethod paymentMethod;

    public List<OrderItemForm> getItems() {
        return items;
    }

    public void setItems(List<OrderItemForm> items) {
        this.items = items;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
