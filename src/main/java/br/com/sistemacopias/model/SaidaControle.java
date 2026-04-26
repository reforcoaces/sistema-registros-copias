package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fluxo_saida")
public class SaidaControle {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "data_hora_registro", nullable = false)
    private LocalDateTime dataHoraRegistro;
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_recorrente", length = 40)
    private CategoriaSaidaRecorrente categoriaRecorrente;
    @Column(name = "descricao_compra", columnDefinition = "TEXT")
    private String descricaoCompra;
    @Column(precision = 19, scale = 2)
    private BigDecimal valor;
    @Column(name = "data_compra_ou_saida")
    private LocalDate dataCompraOuSaida;
    @Column(name = "banco_pagamento", length = 120)
    private String bancoPagamento;
    /** Link (ex.: Mercado Livre) para consulta futura. */
    @Column(name = "link_compra", columnDefinition = "TEXT")
    private String linkCompra;
    /** Onde foi comprado (loja, app, etc.) quando nao e so o link. */
    @Column(name = "origem_compra", length = 300)
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
