package br.com.sistemacopias.model;

/**
 * Despesas que se repetem mensalmente; OUTRO para descricao livre.
 */
public enum CategoriaSaidaRecorrente {
    CONTA_AGUA("Conta de agua"),
    CONTA_LUZ("Conta de luz"),
    INTERNET("Internet"),
    SEGURANCA("Seguranca"),
    ALUGUEL("Aluguel"),
    SEGURO("Seguro"),
    OUTRO("Outra saida (especificar)");

    private final String label;

    CategoriaSaidaRecorrente(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
