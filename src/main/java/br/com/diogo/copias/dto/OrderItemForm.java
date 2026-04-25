package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemForm {
    @NotNull(message = "Selecione um produto")
    private ProductType productType;

    @Min(value = 1, message = "Quantidade deve ser maior ou igual a 1")
    private int quantity = 1;

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
}
