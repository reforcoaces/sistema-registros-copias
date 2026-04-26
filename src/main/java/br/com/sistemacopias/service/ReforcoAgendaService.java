package br.com.sistemacopias.service;

import br.com.sistemacopias.model.AgendaHorarioConfig;
import br.com.sistemacopias.model.AgendaSemana;
import br.com.sistemacopias.repository.AgendaRepository;
import br.com.sistemacopias.support.AgendaGrade;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReforcoAgendaService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

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
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulasDaAgenda(agenda)) {
            String v = textosBrutos == null ? "" : textosBrutos.getOrDefault("t_" + c.id(), "");
            limpo.put(c.id(), v == null ? "" : v.trim());
        }
        agenda.setTextos(limpo);
        agendaRepository.save(agenda);
    }

    /**
     * Atualiza inicio, duracao das aulas e hora limite de termino da ultima faixa.
     * Ao mudar a grade, os textos anteriores sao limpos (ids das celulas mudam).
     */
    public void salvarHorario(String horaInicio, int duracaoMinutos, String horaLimite) {
        try {
            LocalTime.parse(horaInicio.trim(), FMT);
            LocalTime.parse(horaLimite.trim(), FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use horarios no formato HH:mm (ex.: 08:00).");
        }
        if (duracaoMinutos < 15 || duracaoMinutos > 240) {
            throw new IllegalArgumentException("Duracao deve estar entre 15 e 240 minutos");
        }
        AgendaHorarioConfig cfg = new AgendaHorarioConfig();
        cfg.setHoraInicio(horaInicio.trim());
        cfg.setDuracaoMinutos(duracaoMinutos);
        cfg.setHoraLimite(horaLimite.trim());
        AgendaSemana agenda = agendaRepository.load();
        Set<String> idsAntes = AgendaGrade.celulasDaAgenda(agenda).stream()
                .map(AgendaGrade.CelulaDef::id)
                .collect(Collectors.toSet());
        agenda.setHorarioConfig(cfg);
        Set<String> idsDepois = AgendaGrade.celulasDaAgenda(agenda).stream()
                .map(AgendaGrade.CelulaDef::id)
                .collect(Collectors.toSet());
        if (!idsAntes.equals(idsDepois)) {
            agenda.setTextos(new LinkedHashMap<>());
            for (AgendaGrade.CelulaDef c : AgendaGrade.celulasDaAgenda(agenda)) {
                agenda.getTextos().put(c.id(), "");
            }
        }
        agendaRepository.save(agenda);
    }
}
