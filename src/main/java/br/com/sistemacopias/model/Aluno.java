package br.com.sistemacopias.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reforco_aluno")
public class Aluno {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "nome_completo", nullable = false, length = 300)
    private String nomeCompleto;
    @Column(nullable = false)
    private int idade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Escolaridade escolaridade;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pcd", nullable = false, length = 40)
    private TipoPcd tipoPcd = TipoPcd.NAO;

    /** Opcional */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;
    @Column(name = "nome_pai", length = 300)
    private String nomePai;
    @Column(name = "nome_mae", length = 300)
    private String nomeMae;
    @Column(name = "telefone_contato", length = 80)
    private String telefoneContato;
    @Column(name = "objetivo_reforco", columnDefinition = "TEXT")
    private String objetivoReforco;
    @Column(name = "expectativa_pais", columnDefinition = "TEXT")
    private String expectativaPais;

    /** Valor combinado de mensalidade (registado no dia do cadastro quando houver cobranca automatica). */
    @Column(name = "valor_mensalidade", precision = 19, scale = 2)
    private BigDecimal valorMensalidade;
    /** Dia do mes preferido para cobranca (1 a 31). */
    @Column(name = "dia_pagamento_preferido")
    private Integer diaPagamentoPreferido;
    @Enumerated(EnumType.STRING)
    @Column(name = "recorrencia_mensalidade", length = 32)
    private RecorrenciaMensalidade recorrenciaMensalidade;
    /** Proxima data prevista para gerar cobranca automatica (apos confirmacoes). */
    @Column(name = "proxima_cobranca_prevista")
    private LocalDate proximaCobrancaPrevista;

    /** So para ler JSON antigo com campo booleano {@code pcd}; nao e gravado ao salvar. */
    @JsonProperty(value = "pcd", access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private Boolean pcdLegado;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Aluno() {
    }

    @PostLoad
    private void aposCarregar() {
        migrarCampoPcdLegadoSeNecessario();
        migrarMensalidadeLegadoSeNecessario();
    }

    public static Aluno novo(String nomeCompleto, int idade, Escolaridade escolaridade, TipoPcd tipoPcd) {
        Aluno a = new Aluno();
        a.id = UUID.randomUUID().toString();
        a.nomeCompleto = nomeCompleto;
        a.idade = idade;
        a.escolaridade = escolaridade;
        a.tipoPcd = tipoPcd != null ? tipoPcd : TipoPcd.NAO;
        a.createdAt = LocalDateTime.now();
        return a;
    }

    /**
     * Apos ler do ficheiro JSON: converte {@code pcd} legado em {@link TipoPcd} quando necessario.
     * (JSON antigo so tinha booleano {@code pcd}; o campo {@code tipoPcd} pode vir null ou {@link TipoPcd#NAO} por omissao.)
     */
    public void migrarCampoPcdLegadoSeNecessario() {
        if (Boolean.TRUE.equals(pcdLegado) && (tipoPcd == null || tipoPcd == TipoPcd.NAO)) {
            tipoPcd = TipoPcd.ESPECIFICACAO_PENDENTE;
        } else if (tipoPcd == null) {
            tipoPcd = TipoPcd.NAO;
        }
        pcdLegado = null;
    }

    public void migrarMensalidadeLegadoSeNecessario() {
        if (recorrenciaMensalidade == null
                && valorMensalidade != null
                && valorMensalidade.compareTo(BigDecimal.ZERO) > 0) {
            recorrenciaMensalidade = RecorrenciaMensalidade.MENSAL;
        }
    }

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

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public Escolaridade getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(Escolaridade escolaridade) {
        this.escolaridade = escolaridade;
    }

    public TipoPcd getTipoPcd() {
        return tipoPcd != null ? tipoPcd : TipoPcd.NAO;
    }

    public void setTipoPcd(TipoPcd tipoPcd) {
        this.tipoPcd = tipoPcd != null ? tipoPcd : TipoPcd.NAO;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getNomePai() {
        return nomePai;
    }

    public void setNomePai(String nomePai) {
        this.nomePai = nomePai;
    }

    public String getNomeMae() {
        return nomeMae;
    }

    public void setNomeMae(String nomeMae) {
        this.nomeMae = nomeMae;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public String getObjetivoReforco() {
        return objetivoReforco;
    }

    public void setObjetivoReforco(String objetivoReforco) {
        this.objetivoReforco = objetivoReforco;
    }

    public String getExpectativaPais() {
        return expectativaPais;
    }

    public void setExpectativaPais(String expectativaPais) {
        this.expectativaPais = expectativaPais;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getValorMensalidade() {
        return valorMensalidade;
    }

    public void setValorMensalidade(BigDecimal valorMensalidade) {
        this.valorMensalidade = valorMensalidade;
    }

    public Integer getDiaPagamentoPreferido() {
        return diaPagamentoPreferido;
    }

    public void setDiaPagamentoPreferido(Integer diaPagamentoPreferido) {
        this.diaPagamentoPreferido = diaPagamentoPreferido;
    }

    public RecorrenciaMensalidade getRecorrenciaMensalidade() {
        return recorrenciaMensalidade;
    }

    public void setRecorrenciaMensalidade(RecorrenciaMensalidade recorrenciaMensalidade) {
        this.recorrenciaMensalidade = recorrenciaMensalidade;
    }

    public LocalDate getProximaCobrancaPrevista() {
        return proximaCobrancaPrevista;
    }

    public void setProximaCobrancaPrevista(LocalDate proximaCobrancaPrevista) {
        this.proximaCobrancaPrevista = proximaCobrancaPrevista;
    }
}
