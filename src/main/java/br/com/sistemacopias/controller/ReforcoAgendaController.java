package br.com.sistemacopias.controller;

import br.com.sistemacopias.service.ReforcoAgendaService;
import br.com.sistemacopias.support.AgendaGrade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("agenda", agendaService.carregar());
        model.addAttribute("linhasAgenda", AgendaGrade.linhasPorHorario());
        return "reforco/agenda";
    }

    @PostMapping
    public String salvar(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulas()) {
            String key = "t_" + c.id();
            String v = request.getParameter(key);
            map.put(key, v);
        }
        agendaService.salvarTextos(map);
        return "redirect:/reforco/agenda?sucesso=1";
    }
}
