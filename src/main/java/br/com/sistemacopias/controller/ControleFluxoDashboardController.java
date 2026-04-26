package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.FluxoDashboardDto;
import br.com.sistemacopias.service.ControleFluxoService;
import br.com.sistemacopias.support.ChartBarHelper;
import br.com.sistemacopias.support.ChartPieHelper;
import br.com.sistemacopias.support.DashboardVistaUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/controle-fluxo")
public class ControleFluxoDashboardController {
    private final ControleFluxoService controleFluxoService;

    public ControleFluxoDashboardController(ControleFluxoService controleFluxoService) {
        this.controleFluxoService = controleFluxoService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false, defaultValue = "nenhuma") String vistaExtra,
            Model model) {
        YearMonth ym = YearMonth.now();
        if (ano != null && mes != null && mes >= 1 && mes <= 12) {
            try {
                ym = YearMonth.of(ano, mes);
            } catch (Exception ignored) {
                ym = YearMonth.now();
            }
        }
        FluxoDashboardDto fluxo = controleFluxoService.montarDashboard(ym);
        model.addAttribute("fluxo", fluxo);
        model.addAttribute("anoSelecionado", ym.getYear());
        model.addAttribute("mesSelecionado", ym.getMonthValue());
        model.addAttribute("mesReferenciaLabel", ym.format(DateTimeFormatter.ofPattern("MM/yyyy")));

        String vista = DashboardVistaUtil.fluxo(vistaExtra);
        model.addAttribute("vistaExtra", vista);
        if ("pizza_resumo".equals(vista)) {
            ChartPieHelper.buildFromMoneyMap(ChartPieHelper.orderedMap(
                    "Saidas", fluxo.getTotalSaidasMes(),
                    "Entradas (total)", fluxo.getTotalEntradasMes()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("pizza_entradas".equals(vista)) {
            ChartPieHelper.buildFromMoneyMap(ChartPieHelper.orderedMap(
                    "Entradas manuais", fluxo.getTotalEntradasManuaisMes(),
                    "Pedidos (copias)", fluxo.getTotalEntradasPedidosMes()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("barras_resumo".equals(vista)) {
            Map<String, BigDecimal> m = new LinkedHashMap<>();
            m.put("Saidas", fluxo.getTotalSaidasMes());
            m.put("Entradas manuais", fluxo.getTotalEntradasManuaisMes());
            m.put("Pedidos copias", fluxo.getTotalEntradasPedidosMes());
            model.addAttribute("chartBarrasExtra", ChartBarHelper.horizontalFromMoney(m));
        }
        return "controle-fluxo/dashboard";
    }
}
