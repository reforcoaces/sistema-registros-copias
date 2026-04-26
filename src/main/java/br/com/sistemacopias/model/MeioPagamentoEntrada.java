package br.com.sistemacopias.model;

public enum MeioPagamentoEntrada {
    /** Entrada automatica de mensalidade aguardando confirmacao do responsavel. */
    A_CONFIRMAR("A confirmar (mensalidade pendente)"),
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

    /** Meios disponiveis em formularios manuais (nao inclui entradas automaticas). */
    public static MeioPagamentoEntrada[] valoresFormularioManual() {
        return new MeioPagamentoEntrada[]{
                BOLETO, PIX, DINHEIRO, TRANSFERENCIA, CARTAO, OUTRO
        };
    }
}
