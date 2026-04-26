package br.com.sistemacopias.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class FluxoDashboardDto {
    private YearMonth mesReferencia;
    private BigDecimal totalSaidasMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasManuaisMes = BigDecimal.ZERO;
    private BigDecimal totalEntradasPedidosMes = BigDecimal.ZERO;
    private List<MovimentoFluxoLinha> movimentosDoMes = new ArrayList<>();

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

    public BigDecimal getTotalEntradasPedidosMes() {
        return totalEntradasPedidosMes;
    }

    public void setTotalEntradasPedidosMes(BigDecimal totalEntradasPedidosMes) {
        this.totalEntradasPedidosMes = totalEntradasPedidosMes;
    }

    public BigDecimal getTotalEntradasMes() {
        return totalEntradasManuaisMes.add(totalEntradasPedidosMes);
    }

    public BigDecimal getSaldoMes() {
        return getTotalEntradasMes().subtract(totalSaidasMes);
    }

    public List<MovimentoFluxoLinha> getMovimentosDoMes() {
        return movimentosDoMes;
    }

    public void setMovimentosDoMes(List<MovimentoFluxoLinha> movimentosDoMes) {
        this.movimentosDoMes = movimentosDoMes;
    }
}
