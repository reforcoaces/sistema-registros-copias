package br.com.sistemacopias.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Aluno {
    private String id;
    private String nomeCompleto;
    private int idade;
    private Escolaridade escolaridade;
    private TipoPcd tipoPcd = TipoPcd.NAO;

    /** Opcional */
    private Sexo sexo;
    private String nomePai;
    private String nomeMae;
    private String telefoneContato;
    private String objetivoReforco;
    private String expectativaPais;

    /** Valor combinado de mensalidade (registado no dia do cadastro quando houver cobranca automatica). */
    private BigDecimal valorMensalidade;
    /** Dia do mes preferido para cobranca (1 a 31). */
    private Integer diaPagamentoPreferido;
    private RecorrenciaMensalidade recorrenciaMensalidade;
    /** Proxima data prevista para gerar cobranca automatica (apos confirmacoes). */
    private LocalDate proximaCobrancaPrevista;

    /** So para ler JSON antigo com campo booleano {@code pcd}; nao e gravado ao salvar. */
    @JsonProperty(value = "pcd", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean pcdLegado;

    private LocalDateTime createdAt;

    public Aluno() {
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
