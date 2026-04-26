package br.com.sistemacopias.repository;

import br.com.sistemacopias.support.DataFileBackupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Origens de compra adicionadas pelo utilizador (JSON). As sugestoes fixas estao em {@link br.com.sistemacopias.support.OrigensCompraPadrao}.
 */
@Repository
public class OrigemCompraRepository {
    private final Path dataFilePath;
    private final ObjectMapper objectMapper;
    private final DataFileBackupService backupService;

    public OrigemCompraRepository(
            @Value("${app.fluxo.origens-compra.path:data/origens-compra.json}") String path,
            DataFileBackupService backupService) {
        this.dataFilePath = Path.of(path).toAbsolutePath().normalize();
        this.backupService = backupService;
        this.objectMapper = new ObjectMapper();
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public synchronized List<String> findAllCustom() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(dataFilePath);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            List<String> list = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            list.sort(Comparator.comparing(String::toLowerCase));
            return list;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler origens-compra", e);
        }
    }

    /**
     * Guarda na lista persistente se ainda nao existir (comparacao sem distinguir maiusculas).
     */
    public synchronized void addIfAbsentIgnoreCase(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String v = value.trim();
        List<String> all = new ArrayList<>(findAllCustom());
        for (String x : all) {
            if (x.equalsIgnoreCase(v)) {
                return;
            }
        }
        all.add(v);
        all.sort(Comparator.comparing(String::toLowerCase));
        saveAllInternal(all);
    }

    private void saveAllInternal(List<String> all) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), all);
            backupService.mirrorAfterWrite(dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar origens-compra", e);
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
            throw new IllegalStateException("Erro ao preparar origens-compra.json", e);
        }
    }
}
