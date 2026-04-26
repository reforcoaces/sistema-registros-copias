package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.CategoriaSaidaRecorrente;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaidaControleForm {
    private String id;

    @NotNull(message = "Selecione o tipo de despesa")
    private CategoriaSaidaRecorrente categoriaRecorrente;

    private String descricaoCompra;

    @NotNull(message = "Informe o valor")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Informe a data da compra ou saida")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataCompraOuSaida;

    @NotBlank(message = "Indique o banco ou conta utilizada para pagar")
    private String bancoPagamento;

    private String linkCompra;
    private String origemCompra;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
