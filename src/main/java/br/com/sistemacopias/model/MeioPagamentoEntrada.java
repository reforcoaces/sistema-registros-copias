package br.com.sistemacopias.model;

public enum MeioPagamentoEntrada {
    BOLETO("Boleto"),
    PIX("Pix"),
    DINHEIRO("Dinheiro"),
    TRANSFERENCIA("Transferencia bancaria"),
    CARTAO("Cartao"),
    OUTRO("Outro (especificar)");

    private final String label;

    MeioPagamentoEntrada(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
