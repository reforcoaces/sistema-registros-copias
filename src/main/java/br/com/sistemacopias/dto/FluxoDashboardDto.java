package br.com.sistemacopias.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class FluxoDashboardDto {
    private YearMonth mesReferencia;
    private BigDecimal totalSaidasMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasManuaisMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasManuaisConfirmadasMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasManuaisPendentesMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasPedidosMes = BigDecimal.ZERO;
    private List<MovimentoFluxoLinha> movimentosDoMes = new ArrayList<>();
    /** Id do aluno quando o dashboard filtra movimentos desse aluno (null = sem filtro). */
    private String filtroAlunoId;
    private String filtroAlunoNome;
    /** Total de entradas manuais confirmadas (pagamento aluno) do aluno filtrado no mes. */
    private BigDecimal totalEntradasAlunoFiltradoConfirmadasMes = BigDecimal.ZERO;

    public YearMonth getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(YearMonth mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public BigDecimal getTotalSaidasMes() {
        return totalSaidasMes;
    }

    public void setTotalSaidasMes(BigDecimal totalSaidasMes) {
        this.totalSaidasMes = totalSaidasMes;
    }

    public BigDecimal getTotalEntradasManuaisMes() {
        return totalEntradasManuaisMes;
    }

    public void setTotalEntradasManuaisMes(BigDecimal totalEntradasManuaisMes) {
        this.totalEntradasManuaisMes = totalEntradasManuaisMes;
    }

    public BigDecimal getTotalEntradasManuaisConfirmadasMes() {
        return totalEntradasManuaisConfirmadasMes;
    }

    public void setTotalEntradasManuaisConfirmadasMes(BigDecimal totalEntradasManuaisConfirmadasMes) {
        this.totalEntradasManuaisConfirmadasMes = totalEntradasManuaisConfirmadasMes;
    }

    public BigDecimal getTotalEntradasManuaisPendentesMes() {
        return totalEntradasManuaisPendentesMes;
    }

    public void setTotalEntradasManuaisPendentesMes(BigDecimal totalEntradasManuaisPendentesMes) {
        this.totalEntradasManuaisPendentesMes = totalEntradasManuaisPendentesMes;
    }

    public BigDecimal getTotalEntradasPedidosMes() {
        return totalEntradasPedidosMes;
    }

    public void setTotalEntradasPedidosMes(BigDecimal totalEntradasPedidosMes) {
        this.totalEntradasPedidosMes = totalEntradasPedidosMes;
    }

    /** Entradas efetivas (confirmadas manuais + pedidos de copias). */
    public BigDecimal getTotalEntradasEfetivasMes() {
        return totalEntradasManuaisConfirmadasMes.add(totalEntradasPedidosMes);
    }

    /** Soma confirmadas + pendentes + pedidos (visao bruta das entradas manuais). */
    public BigDecimal getTotalEntradasMes() {
        return totalEntradasManuaisMes.add(totalEntradasPedidosMes);
    }

    /** Saldo considerando apenas entradas confirmadas e pedidos. */
    public BigDecimal getSaldoMes() {
        return getTotalEntradasEfetivasMes().subtract(totalSaidasMes);
    }

    public List<MovimentoFluxoLinha> getMovimentosDoMes() {
        return movimentosDoMes;
    }

    public void setMovimentosDoMes(List<MovimentoFluxoLinha> movimentosDoMes) {
        this.movimentosDoMes = movimentosDoMes;
    }

    public String getFiltroAlunoId() {
        return filtroAlunoId;
    }

    public void setFiltroAlunoId(String filtroAlunoId) {
        this.filtroAlunoId = filtroAlunoId;
    }

    public String getFiltroAlunoNome() {
        return filtroAlunoNome;
    }

    public void setFiltroAlunoNome(String filtroAlunoNome) {
        this.filtroAlunoNome = filtroAlunoNome;
    }

    public BigDecimal getTotalEntradasAlunoFiltradoConfirmadasMes() {
        return totalEntradasAlunoFiltradoConfirmadasMes;
    }

    public void setTotalEntradasAlunoFiltradoConfirmadasMes(BigDecimal totalEntradasAlunoFiltradoConfirmadasMes) {
        this.totalEntradasAlunoFiltradoConfirmadasMes = totalEntradasAlunoFiltradoConfirmadasMes;
    }
}
