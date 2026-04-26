package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.MeioPagamentoEntrada;
import br.com.sistemacopias.model.TipoEntradaControle;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class EntradaControleForm {
    private String id;

    @NotNull(message = "Selecione o tipo de entrada")
    private TipoEntradaControle tipo;

    private String alunoId;

    private String descricaoLivre;

    @NotNull(message = "Informe o valor")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Selecione o meio da entrada")
    private MeioPagamentoEntrada meioPagamento;

    private String meioPagamentoOutro;

    @AssertTrue(message = "Especifique o meio quando escolher \"Outro\"")
    public boolean isMeioOutroPreenchido() {
        if (meioPagamento == null) {
            return false;
        }
        if (meioPagamento == MeioPagamentoEntrada.A_CONFIRMAR) {
            return true;
        }
        if (meioPagamento == MeioPagamentoEntrada.OUTRO) {
            return meioPagamentoOutro != null && !meioPagamentoOutro.isBlank();
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoEntradaControle getTipo() {
        return tipo;
    }

    public void setTipo(TipoEntradaControle tipo) {
        this.tipo = tipo;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public String getDescricaoLivre() {
        return descricaoLivre;
    }

    public void setDescricaoLivre(String descricaoLivre) {
        this.descricaoLivre = descricaoLivre;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public MeioPagamentoEntrada getMeioPagamento() {
        return meioPagamento;
    }

    public void setMeioPagamento(MeioPagamentoEntrada meioPagamento) {
        this.meioPagamento = meioPagamento;
    }

    public String getMeioPagamentoOutro() {
        return meioPagamentoOutro;
    }

    public void setMeioPagamentoOutro(String meioPagamentoOutro) {
        this.meioPagamentoOutro = meioPagamentoOutro;
    }
}
