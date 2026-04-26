package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.AgendaSemana;
import br.com.sistemacopias.support.AgendaGrade;
import br.com.sistemacopias.support.DataFileBackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class AgendaRepository {
    private final Path dataFilePath;
    private final ObjectMapper objectMapper;
    private final DataFileBackupService backupService;

    public AgendaRepository(
            @Value("${app.reforco.agenda.path:data/agenda-semana.json}") String path,
            DataFileBackupService backupService) {
        this.dataFilePath = Path.of(path).toAbsolutePath().normalize();
        this.backupService = backupService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public synchronized AgendaSemana load() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(dataFilePath);
            if (bytes.length == 0) {
                return agendaVazia();
            }
            AgendaSemana a = objectMapper.readValue(bytes, AgendaSemana.class);
            if (a.getTextos() == null) {
                a.setTextos(new LinkedHashMap<>());
            }
            for (AgendaGrade.CelulaDef c : AgendaGrade.celulas()) {
                a.getTextos().putIfAbsent(c.id(), "");
            }
            return a;
        } catch (Exception e) {
            return agendaVazia();
        }
    }

    public synchronized void save(AgendaSemana agenda) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), agenda);
            backupService.mirrorAfterWrite(dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar agenda", e);
        }
    }

    private static AgendaSemana agendaVazia() {
        AgendaSemana a = new AgendaSemana();
        Map<String, String> m = new LinkedHashMap<>();
        for (AgendaGrade.CelulaDef c : AgendaGrade.celulas()) {
            m.put(c.id(), "");
        }
        a.setTextos(m);
        return a;
    }

    private void ensureFileExists() {
        try {
            Path parent = dataFilePath.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(dataFilePath)) {
                AgendaSemana empty = agendaVazia();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), empty);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao preparar agenda-semana.json", e);
        }
    }
}
