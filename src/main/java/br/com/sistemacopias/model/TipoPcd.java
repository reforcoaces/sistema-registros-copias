package br.com.sistemacopias.model;

/**
 * Condicao PCD do aluno (substitui o antigo booleano pcd).
 */
public enum TipoPcd {
    NAO("Nao e PCD"),
    TEA("TEA"),
    DOWN("Down (sindrome de Down)"),
    /** Registos antigos com pcd=true sem tipo; o utilizador deve escolher TEA ou Down. */
    ESPECIFICACAO_PENDENTE("PCD — escolha TEA ou Down");

    private final String label;

    TipoPcd(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
