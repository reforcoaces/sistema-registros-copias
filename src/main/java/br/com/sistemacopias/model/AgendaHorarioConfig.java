package br.com.sistemacopias.model;

import java.util.Objects;

/**
 * Parametros da grade horaria da agenda (segunda a sexta, faixas consecutivas sem sobreposicao).
 * Valores iguais ao padrao historico (8h, 1h15, limite 18h) usam a grade predefinida do sistema.
 */
public class AgendaHorarioConfig {
    private String horaInicio = "08:00";
    private int duracaoMinutos = 75;
    private String horaLimite = "18:00";

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getHoraLimite() {
        return horaLimite;
    }

    public void setHoraLimite(String horaLimite) {
        this.horaLimite = horaLimite;
    }

    public boolean isPadraoHistorico() {
        return normalizaHora(horaInicio).equals("08:00")
                && duracaoMinutos == 75
                && normalizaHora(horaLimite).equals("18:00");
    }

    private static String normalizaHora(String h) {
        if (h == null || h.isBlank()) {
            return "";
        }
        String t = h.trim();
        if (t.length() == 5 && t.charAt(2) == ':') {
            return t;
        }
        return t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgendaHorarioConfig that = (AgendaHorarioConfig) o;
        return duracaoMinutos == that.duracaoMinutos
                && Objects.equals(normalizaHora(horaInicio), normalizaHora(that.horaInicio))
                && Objects.equals(normalizaHora(horaLimite), normalizaHora(that.horaLimite));
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizaHora(horaInicio), duracaoMinutos, normalizaHora(horaLimite));
    }
}
