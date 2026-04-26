package br.com.sistemacopias.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SaidaControle {
    private String id;
    private LocalDateTime dataHoraRegistro;
    private CategoriaSaidaRecorrente categoriaRecorrente;
    private String descricaoCompra;
    private BigDecimal valor;
    private LocalDate dataCompraOuSaida;
    private String bancoPagamento;
    /** Link (ex.: Mercado Livre) para consulta futura. */
    private String linkCompra;
    /** Onde foi comprado (loja, app, etc.) quando nao e so o link. */
    private String origemCompra;

    public SaidaControle() {
    }

    public static SaidaControle nova(String descricaoCompra, BigDecimal valor, LocalDate dataCompraOuSaida, String bancoPagamento) {
        SaidaControle s = new SaidaControle();
        s.id = UUID.randomUUID().toString();
        s.dataHoraRegistro = LocalDateTime.now();
        s.descricaoCompra = descricaoCompra;
        s.valor = valor;
        s.dataCompraOuSaida = dataCompraOuSaida;
        s.bancoPagamento = bancoPagamento;
        return s;
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

    public CategoriaSaidaRecorrente getCategoriaRecorrente() {
        return categoriaRecorrente;
    }

    public void setCategoriaRecorrente(CategoriaSaidaRecorrente categoriaRecorrente) {
        this.categoriaRecorrente = categoriaRecorrente;
    }

    public String getDescricaoCompra() {
        return descricaoCompra;
    }

    public void setDescricaoCompra(String descricaoCompra) {
        this.descricaoCompra = descricaoCompra;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataCompraOuSaida() {
        return dataCompraOuSaida;
    }

    public void setDataCompraOuSaida(LocalDate dataCompraOuSaida) {
        this.dataCompraOuSaida = dataCompraOuSaida;
    }

    public String getBancoPagamento() {
        return bancoPagamento;
    }

    public void setBancoPagamento(String bancoPagamento) {
        this.bancoPagamento = bancoPagamento;
    }

    public String getLinkCompra() {
        return linkCompra;
    }

    public void setLinkCompra(String linkCompra) {
        this.linkCompra = linkCompra;
    }

    public String getOrigemCompra() {
        return origemCompra;
    }

    public void setOrigemCompra(String origemCompra) {
        this.origemCompra = origemCompra;
    }
}
