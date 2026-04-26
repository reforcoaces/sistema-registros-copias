package br.com.sistemacopias.service;

import br.com.sistemacopias.model.AgendaSemana;
import br.com.sistemacopias.repository.AgendaRepository;
import br.com.sistemacopias.support.AgendaGrade;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReforcoAgendaService {
    private final AgendaRepository agendaRepository;

    public ReforcoAgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public AgendaSemana carregar() {
        return agendaRepository.load();
    }

    public void salvarTextos(Map<String, String> textosBrutos) {
        AgendaSemana agenda = agendaRepository.load();
        Map<String, String> limpo = new LinkedHashMap<>();
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulas()) {
            String v = textosBrutos == null ? "" : textosBrutos.getOrDefault("t_" + c.id(), "");
            limpo.put(c.id(), v == null ? "" : v.trim());
        }
        agenda.setTextos(limpo);
        agendaRepository.save(agenda);
    }
}
