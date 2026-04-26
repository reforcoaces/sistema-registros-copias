package br.com.sistemacopias.model;

public enum SituacaoEntrada {
    PENDENTE("Pendente de confirmacao"),
    CONFIRMADA("Confirmada (entrada efetiva)");

    private final String label;

    SituacaoEntrada(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
