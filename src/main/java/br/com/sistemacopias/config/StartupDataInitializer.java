package br.com.sistemacopias.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class StartupDataInitializer implements CommandLineRunner {
    private final String storagePath;
    private final String alunosPath;
    private final String atividadesPath;
    private final String agendaPath;
    private final String saidasFluxoPath;
    private final String entradasFluxoPath;

    public StartupDataInitializer(
            @Value("${app.storage.path:data/orders.json}") String storagePath,
            @Value("${app.reforco.alunos.path:data/alunos.json}") String alunosPath,
            @Value("${app.reforco.atividades.path:data/atividades.json}") String atividadesPath,
            @Value("${app.reforco.agenda.path:data/agenda-semana.json}") String agendaPath,
            @Value("${app.fluxo.saidas.path:data/saidas.json}") String saidasFluxoPath,
            @Value("${app.fluxo.entradas.path:data/entradas.json}") String entradasFluxoPath) {
        this.storagePath = storagePath;
        this.alunosPath = alunosPath;
        this.atividadesPath = atividadesPath;
        this.agendaPath = agendaPath;
        this.saidasFluxoPath = saidasFluxoPath;
        this.entradasFluxoPath = entradasFluxoPath;
    }

    @Override
    public void run(String... args) throws Exception {
        ensureJsonFile(storagePath, "[]");
        ensureJsonFile(alunosPath, "[]");
        ensureJsonFile(atividadesPath, "[]");
        ensureJsonFile(saidasFluxoPath, "[]");
        ensureJsonFile(entradasFluxoPath, "[]");
        ensureAgendaFile();
    }

    private void ensureJsonFile(String relative, String defaultContent) throws IOException {
        Path path = Path.of(relative);
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(path)) {
            Files.writeString(path, defaultContent);
        }
    }

    private void ensureAgendaFile() throws IOException {
        Path path = Path.of(agendaPath);
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(path)) {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            var root = om.createObjectNode();
            var textos = om.createObjectNode();
            root.set("textos", textos);
            om.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
        }
    }
}
