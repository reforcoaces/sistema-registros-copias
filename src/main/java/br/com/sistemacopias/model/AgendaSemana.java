package br.com.sistemacopias.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Textos livres por celula da grade semanal (segunda a sexta, blocos de 1h15).
 */
public class AgendaSemana {
    private Map<String, String> textos = new LinkedHashMap<>();

    public Map<String, String> getTextos() {
        return textos;
    }

    public void setTextos(Map<String, String> textos) {
        this.textos = textos != null ? textos : new LinkedHashMap<>();
    }
}
