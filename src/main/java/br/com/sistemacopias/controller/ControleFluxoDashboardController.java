package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.FluxoDashboardDto;
import br.com.sistemacopias.repository.AlunoRepository;
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
    private final AlunoRepository alunoRepository;

    public ControleFluxoDashboardController(ControleFluxoService controleFluxoService, AlunoRepository alunoRepository) {
        this.controleFluxoService = controleFluxoService;
        this.alunoRepository = alunoRepository;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) String alunoId,
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
        String filtroAluno = alunoId != null && !alunoId.isBlank() ? alunoId.trim() : null;
        FluxoDashboardDto fluxo = controleFluxoService.montarDashboard(ym, filtroAluno);
        model.addAttribute("fluxo", fluxo);
        model.addAttribute("anoSelecionado", ym.getYear());
        model.addAttribute("mesSelecionado", ym.getMonthValue());
        model.addAttribute("mesReferenciaLabel", ym.format(DateTimeFormatter.ofPattern("MM/yyyy")));
        model.addAttribute("alunosFiltro", alunoRepository.findAll().stream()
                .sorted((a, b) -> a.getNomeCompleto().compareToIgnoreCase(b.getNomeCompleto()))
                .toList());
        model.addAttribute("alunoFiltroSelecionado", filtroAluno);

        String vista = DashboardVistaUtil.fluxo(vistaExtra);
        model.addAttribute("vistaExtra", vista);
        if ("pizza_resumo".equals(vista)) {
            ChartPieHelper.buildFromMoneyMap(ChartPieHelper.orderedMap(
                    "Saidas", fluxo.getTotalSaidasMes(),
                    "Entradas efetivas", fluxo.getTotalEntradasEfetivasMes()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("pizza_entradas".equals(vista)) {
            ChartPieHelper.buildFromMoneyMap(ChartPieHelper.orderedTriple(
                    "Entradas manuais (confirmadas)", fluxo.getTotalEntradasManuaisConfirmadasMes(),
                    "Entradas manuais (pendentes)", fluxo.getTotalEntradasManuaisPendentesMes(),
                    "Pedidos (copias)", fluxo.getTotalEntradasPedidosMes()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("barras_resumo".equals(vista)) {
            Map<String, BigDecimal> m = new LinkedHashMap<>();
            m.put("Saidas", fluxo.getTotalSaidasMes());
            m.put("Entradas man. confirmadas", fluxo.getTotalEntradasManuaisConfirmadasMes());
            m.put("Entradas man. pendentes", fluxo.getTotalEntradasManuaisPendentesMes());
            m.put("Pedidos copias", fluxo.getTotalEntradasPedidosMes());
            model.addAttribute("chartBarrasExtra", ChartBarHelper.horizontalFromMoney(m));
        }
        return "controle-fluxo/dashboard";
    }
}
