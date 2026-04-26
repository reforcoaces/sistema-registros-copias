package br.com.sistemacopias.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class EntradaControle {
    private String id;
    private LocalDateTime dataHoraRegistro;
    private TipoEntradaControle tipo;
    private String alunoId;
    private String descricaoLivre;
    private BigDecimal valor;
    private MeioPagamentoEntrada meioPagamento;
    private String meioPagamentoOutro;

    public EntradaControle() {
    }

    public static EntradaControle nova(
            TipoEntradaControle tipo,
            String alunoId,
            String descricaoLivre,
            BigDecimal valor,
            MeioPagamentoEntrada meioPagamento,
            String meioPagamentoOutro) {
        EntradaControle e = new EntradaControle();
        e.id = UUID.randomUUID().toString();
        e.dataHoraRegistro = LocalDateTime.now();
        e.tipo = tipo;
        e.alunoId = alunoId;
        e.descricaoLivre = descricaoLivre;
        e.valor = valor;
        e.meioPagamento = meioPagamento;
        e.meioPagamentoOutro = meioPagamentoOutro;
        return e;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDataHoraRegistro() {
        return dataHoraRegistro;
    }

    public void setDataHoraRegistro(LocalDateTime dataHoraRegistro) {
        this.dataHoraRegistro = dataHoraRegistro;
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

    /** Rotulo do meio para listagens (inclui texto se OUTRO). */
    public String getMeioPagamentoResumo() {
        if (meioPagamento == null) {
            return "";
        }
        if (meioPagamento == MeioPagamentoEntrada.OUTRO && meioPagamentoOutro != null && !meioPagamentoOutro.isBlank()) {
            return meioPagamentoOutro.trim();
        }
        return meioPagamento.getLabel();
    }
}
