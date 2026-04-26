package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.Escolaridade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

public class AlunoForm {
    private String id;

    @NotBlank(message = "Informe o nome completo")
    private String nomeCompleto;

    @NotNull(message = "Informe a idade")
    @Min(value = 1, message = "Idade invalida")
    @Max(value = 120, message = "Idade invalida")
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    private Integer idade;

    @NotNull(message = "Selecione a escolaridade")
    private Escolaridade escolaridade;

    private boolean pcd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Escolaridade getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(Escolaridade escolaridade) {
        this.escolaridade = escolaridade;
    }

    public boolean isPcd() {
        return pcd;
    }

    public void setPcd(boolean pcd) {
        this.pcd = pcd;
    }
}
