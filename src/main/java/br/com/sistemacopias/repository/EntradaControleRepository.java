package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.EntradaControle;
import br.com.sistemacopias.support.DataFileBackupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class EntradaControleRepository {
    private final Path dataFilePath;
    private final ObjectMapper objectMapper;
    private final DataFileBackupService backupService;

    public EntradaControleRepository(
            @Value("${app.fluxo.entradas.path:data/entradas.json}") String path,
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

    public synchronized List<EntradaControle> findAll() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(dataFilePath);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            List<EntradaControle> list = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            list.sort(Comparator.comparing(EntradaControle::getDataHoraRegistro).reversed());
            return list;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler entradas", e);
        }
    }

    public synchronized Optional<EntradaControle> findById(String id) {
        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public synchronized void save(EntradaControle novo) {
        List<EntradaControle> all = new ArrayList<>(findAll());
        all.add(novo);
        saveAllInternal(all);
    }

    public synchronized void update(EntradaControle atualizado) {
        List<EntradaControle> all = new ArrayList<>(findAll());
        boolean ok = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(atualizado.getId())) {
                all.set(i, atualizado);
                ok = true;
                break;
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("Entrada nao encontrada");
        }
        saveAllInternal(all);
    }

    private void saveAllInternal(List<EntradaControle> all) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), all);
            backupService.mirrorAfterWrite(dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar entradas", e);
        }
    }

    private void ensureFileExists() {
        try {
            Path parent = dataFilePath.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(dataFilePath)) {
                Files.writeString(dataFilePath, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao preparar entradas.json", e);
        }
    }
}
