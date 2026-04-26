package br.com.sistemacopias.model;

public enum RecorrenciaMensalidade {
    SEMANAL("Semanal", 0, 1),
    QUINZENAL("Quinzenal (a cada 2 semanas)", 0, 2),
    MENSAL("Mensal", 1, 0),
    BIMESTRAL("Bimestral", 2, 0),
    TRIMESTRAL("Trimestral", 3, 0),
    SEMESTRAL("Semestral", 6, 0),
    ANUAL("Anual", 12, 0);

    private final String label;
    private final int meses;
    private final int semanas;

    RecorrenciaMensalidade(String label, int meses, int semanas) {
        this.label = label;
        this.meses = meses;
        this.semanas = semanas;
    }

    public String getLabel() {
        return label;
    }

    /** Intervalo em meses (0 se for apenas por semanas). */
    public int getMeses() {
        return meses;
    }

    public int getSemanas() {
        return semanas;
    }

    public boolean isPorSemanas() {
        return semanas > 0;
    }
}
