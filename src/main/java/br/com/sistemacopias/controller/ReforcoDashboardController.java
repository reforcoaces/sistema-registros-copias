package br.com.sistemacopias.controller;

import br.com.sistemacopias.service.ReforcoDashboardService;
import br.com.sistemacopias.support.AgendaGrade;
import br.com.sistemacopias.support.ChartBarHelper;
import br.com.sistemacopias.support.ChartPieHelper;
import br.com.sistemacopias.support.DashboardVistaUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/reforco")
public class ReforcoDashboardController {
    private final ReforcoDashboardService dashboardService;

    public ReforcoDashboardController(ReforcoDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(
            @RequestParam(required = false, defaultValue = "nenhuma") String vistaExtra,
            Model model) {
        var dash = dashboardService.montar();
        model.addAttribute("dash", dash);
        model.addAttribute("linhasAgenda", AgendaGrade.linhasPorHorario());

        String vista = DashboardVistaUtil.reforco(vistaExtra);
        model.addAttribute("vistaExtra", vista);
        if ("pizza_escolaridade".equals(vista)) {
            ChartPieHelper.buildFromCounts(new LinkedHashMap<>(dash.getAlunosPorEscolaridade()))
                    .ifPresent(p -> model.addAttribute("chartPieExtra", p));
        } else if ("barras_pcd".equals(vista)) {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("Alunos PCD", dash.getAlunosPcd());
            m.put("Alunos nao PCD", Math.max(0, dash.getTotalAlunos() - dash.getAlunosPcd()));
            model.addAttribute("chartBarrasExtra", ChartBarHelper.horizontalFromCounts(m));
        }
        return "reforco/dashboard";
    }
}
