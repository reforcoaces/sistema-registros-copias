package br.com.sistemacopias.controller;

import br.com.sistemacopias.service.ReforcoDashboardService;
import br.com.sistemacopias.support.AgendaGrade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reforco")
public class ReforcoDashboardController {
    private final ReforcoDashboardService dashboardService;

    public ReforcoDashboardController(ReforcoDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("dash", dashboardService.montar());
        model.addAttribute("linhasAgenda", AgendaGrade.linhasPorHorario());
        return "reforco/dashboard";
    }
}
