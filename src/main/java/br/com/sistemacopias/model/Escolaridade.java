package br.com.sistemacopias.model;

/**
 * Nivel escolar atual do aluno (uma opcao).
 */
public enum Escolaridade {
    PRE_ESCOLAR("Pre-escolar"),
    ANO_1_EF("1 ano EF"),
    ANO_2_EF("2 ano EF"),
    ANO_3_EF("3 ano EF"),
    ANO_4_EF("4 ano EF"),
    ANO_5_EF("5 ano EF"),
    ANO_6_EF("6 ano EF"),
    ANO_7_EF("7 ano EF"),
    ANO_8_EF("8 ano EF"),
    ANO_9_EF("9 ano EF"),
    ANO_1_EM("1 ano EM"),
    ANO_2_EM("2 ano EM"),
    ANO_3_EM("3 ano EM");

    private final String label;

    Escolaridade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
