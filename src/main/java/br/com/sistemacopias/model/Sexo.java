package br.com.sistemacopias.model;

public enum Sexo {
    MASCULINO("Masculino"),
    FEMININO("Feminino");

    private final String label;

    Sexo(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
