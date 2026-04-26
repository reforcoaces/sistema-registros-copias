package br.com.sistemacopias.controller;

import br.com.sistemacopias.model.AgendaHorarioConfig;
import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.service.ReforcoAgendaService;
import br.com.sistemacopias.support.AgendaGrade;
import br.com.sistemacopias.support.ReforcoAccess;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reforco/agenda")
public class ReforcoAgendaController {
    private final ReforcoAgendaService agendaService;

    public ReforcoAgendaController(ReforcoAgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    public String pagina(Model model) {
        var agenda = agendaService.carregar();
        model.addAttribute("agenda", agenda);
        model.addAttribute("linhasAgenda", AgendaGrade.linhasPorHorario(AgendaGrade.celulasDaAgenda(agenda)));
        AgendaHorarioConfig cfg = agenda.getHorarioConfig();
        if (cfg == null) {
            cfg = new AgendaHorarioConfig();
        }
        model.addAttribute("horarioHoraInicio", cfg.getHoraInicio());
        model.addAttribute("horarioDuracao", cfg.getDuracaoMinutos());
        model.addAttribute("horarioHoraLimite", cfg.getHoraLimite());
        return "reforco/agenda";
    }

    @PostMapping
    public String salvar(HttpServletRequest request) {
        var agenda = agendaService.carregar();
        Map<String, String> map = new HashMap<>();
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulasDaAgenda(agenda)) {
            String key = "t_" + c.id();
            String v = request.getParameter(key);
            map.put(key, v);
        }
        agendaService.salvarTextos(map);
        return "redirect:/reforco/agenda?sucesso=1";
    }

    @PostMapping("/horario")
    public String salvarHorario(
            @RequestParam String horaInicio,
            @RequestParam int duracaoMinutos,
            @RequestParam String horaLimite,
            HttpSession session,
            Model model) {
        AppUser user = (AppUser) session.getAttribute("loggedUser");
        if (!ReforcoAccess.podeEditarHorarioAgenda(user)) {
            return "redirect:/reforco/agenda?horarioNegado=1";
        }
        try {
            agendaService.salvarHorario(horaInicio, duracaoMinutos, horaLimite);
        } catch (Exception e) {
            model.addAttribute("erroHorario", e.getMessage() != null ? e.getMessage() : "Dados invalidos");
            var agenda = agendaService.carregar();
            model.addAttribute("agenda", agenda);
            model.addAttribute("linhasAgenda", AgendaGrade.linhasPorHorario(AgendaGrade.celulasDaAgenda(agenda)));
            model.addAttribute("horarioHoraInicio", horaInicio);
            model.addAttribute("horarioDuracao", duracaoMinutos);
            model.addAttribute("horarioHoraLimite", horaLimite);
            return "reforco/agenda";
        }
        return "redirect:/reforco/agenda?horarioOk=1";
    }
}
