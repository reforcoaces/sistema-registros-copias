package br.com.sistemacopias.dto;

import jakarta.validation.constraints.NotBlank;

public class AtividadeForm {
    @NotBlank(message = "Descreva a atividade")
    private String texto;

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
