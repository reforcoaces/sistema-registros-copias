package br.com.sistemacopias.model;

public enum PaymentMethod {
    PIX("Pix"),
    DEBITO("Debito"),
    CREDITO("Credito"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    PaymentMethod(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
