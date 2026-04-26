package br.com.sistemacopias.model;

public enum TipoEntradaControle {
    PAGAMENTO_ALUNO("Pagamento de aluno (mensalidade)"),
    TEXTO_LIVRE("Outra entrada (texto livre)");

    private final String label;

    TipoEntradaControle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
