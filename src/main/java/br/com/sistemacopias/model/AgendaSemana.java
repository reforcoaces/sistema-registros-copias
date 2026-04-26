package br.com.sistemacopias.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Textos livres por celula da grade semanal e opcional configuracao de horarios.
 */
public class AgendaSemana {
    private Map<String, String> textos = new LinkedHashMap<>();
    /** Quando null ou equivalente ao padrao, a grade segue 8h-18h em blocos de 1h15. */
    private AgendaHorarioConfig horarioConfig;

    public Map<String, String> getTextos() {
        return textos;
    }

    public void setTextos(Map<String, String> textos) {
        this.textos = textos != null ? textos : new LinkedHashMap<>();
    }

    public AgendaHorarioConfig getHorarioConfig() {
        return horarioConfig;
    }

    public void setHorarioConfig(AgendaHorarioConfig horarioConfig) {
        this.horarioConfig = horarioConfig;
    }
}
