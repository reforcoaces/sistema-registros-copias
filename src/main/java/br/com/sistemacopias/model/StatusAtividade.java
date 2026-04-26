package br.com.sistemacopias.model;

public enum StatusAtividade {
    A_FAZER("A fazer"),
    FAZENDO("Fazendo"),
    FINALIZADO("Finalizado");

    private final String label;

    StatusAtividade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
