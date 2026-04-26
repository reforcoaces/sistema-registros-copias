package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.Escolaridade;
import br.com.sistemacopias.model.RecorrenciaMensalidade;
import br.com.sistemacopias.model.Sexo;
import br.com.sistemacopias.model.TipoPcd;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

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

    @NotNull(message = "Indique a condicao PCD (ou Nao e PCD)")
    private TipoPcd tipoPcd = TipoPcd.NAO;

    @AssertTrue(message = "Se o aluno e PCD, selecione TEA ou Down")
    public boolean isTipoPcdDefinido() {
        return tipoPcd != TipoPcd.ESPECIFICACAO_PENDENTE;
    }

    @DecimalMin(value = "0.0", inclusive = true, message = "Valor de mensalidade invalido")
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    private BigDecimal valorMensalidade;

    @Min(value = 1, message = "Dia do pagamento entre 1 e 31")
    @Max(value = 31, message = "Dia do pagamento entre 1 e 31")
    private Integer diaPagamentoPreferido;

    private RecorrenciaMensalidade recorrenciaMensalidade;

    @AssertTrue(message = "Com mensalidade informada, indique o dia combinado para pagamento e a recorrencia")
    public boolean isMensalidadeCoerente() {
        if (valorMensalidade == null || valorMensalidade.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return diaPagamentoPreferido != null && recorrenciaMensalidade != null;
    }

    private Sexo sexo;
    private String nomePai;
    private String nomeMae;
    private String telefoneContato;
    private String objetivoReforco;
    private String expectativaPais;

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

    public TipoPcd getTipoPcd() {
        return tipoPcd;
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
}
