package br.com.sistemacopias.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimentoFluxoLinha {
    private String id;
    private LocalDateTime dataHora;
    private boolean saida;
    private String descricao;
    private BigDecimal valor;
    private String detalhe;

    public MovimentoFluxoLinha() {
    }

    public MovimentoFluxoLinha(String id, LocalDateTime dataHora, boolean saida, String descricao, BigDecimal valor, String detalhe) {
        this.id = id;
        this.dataHora = dataHora;
        this.saida = saida;
        this.descricao = descricao;
        this.valor = valor;
        this.detalhe = detalhe;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public boolean isSaida() {
        return saida;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDetalhe() {
        return detalhe;
    }
}
