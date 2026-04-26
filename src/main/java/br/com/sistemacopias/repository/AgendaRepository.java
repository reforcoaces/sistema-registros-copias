package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.AgendaSemana;
import br.com.sistemacopias.model.ReforcoAgendaSingleton;
import br.com.sistemacopias.support.AgendaGrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class AgendaRepository {

    private final ReforcoAgendaSingletonRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public AgendaRepository(ReforcoAgendaSingletonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public AgendaSemana load() {
        return jpaRepository.findById(ReforcoAgendaSingleton.SINGLETON_ID)
                .map(row -> parseOrVazia(row.getJsonPayload()))
                .orElseGet(AgendaRepository::agendaVazia);
    }

    public void save(AgendaSemana agenda) {
        try {
            String json = objectMapper.writeValueAsString(agenda != null ? agenda : agendaVazia());
            ReforcoAgendaSingleton row = jpaRepository.findById(ReforcoAgendaSingleton.SINGLETON_ID)
                    .orElseGet(() -> new ReforcoAgendaSingleton(ReforcoAgendaSingleton.SINGLETON_ID, json));
            row.setJsonPayload(json);
            jpaRepository.save(row);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao salvar agenda", e);
        }
    }

    private AgendaSemana parseOrVazia(String json) {
        try {
            if (json == null || json.isBlank()) {
                return agendaVazia();
            }
            AgendaSemana a = objectMapper.readValue(json, AgendaSemana.class);
            if (a.getTextos() == null) {
                a.setTextos(new LinkedHashMap<>());
            }
            for (AgendaGrade.CelulaDef c : AgendaGrade.celulasDaAgenda(a)) {
                a.getTextos().putIfAbsent(c.id(), "");
            }
            return a;
        } catch (Exception e) {
            return agendaVazia();
        }
    }

    private static AgendaSemana agendaVazia() {
        AgendaSemana a = new AgendaSemana();
        Map<String, String> m = new LinkedHashMap<>();
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulasDaAgenda(a)) {
            m.put(c.id(), "");
        }
        a.setTextos(m);
        return a;
    }
}
