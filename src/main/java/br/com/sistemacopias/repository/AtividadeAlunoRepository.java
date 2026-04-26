package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.AtividadeAluno;
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
import java.util.stream.Collectors;

@Repository
public class AtividadeAlunoRepository {
    private final Path dataFilePath;
    private final ObjectMapper objectMapper;
    private final DataFileBackupService backupService;

    public AtividadeAlunoRepository(
            @Value("${app.reforco.atividades.path:data/atividades.json}") String path,
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

    public synchronized List<AtividadeAluno> findAll() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(dataFilePath);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            List<AtividadeAluno> list = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            for (AtividadeAluno a : list) {
                a.migrarStatusLegadoSeNecessario();
            }
            return list;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler atividades", e);
        }
    }

    public synchronized List<AtividadeAluno> findByAlunoId(String alunoId) {
        return findAll().stream()
                .filter(a -> a.getAlunoId().equals(alunoId))
                .sorted(Comparator.comparing(AtividadeAluno::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public synchronized Optional<AtividadeAluno> findById(String id) {
        return findAll().stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public synchronized void save(AtividadeAluno nova) {
        List<AtividadeAluno> all = new ArrayList<>(findAll());
        all.add(nova);
        saveAllInternal(all);
    }

    public synchronized void update(AtividadeAluno atualizada) {
        List<AtividadeAluno> all = new ArrayList<>(findAll());
        boolean ok = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(atualizada.getId())) {
                all.set(i, atualizada);
                ok = true;
                break;
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("Atividade nao encontrada");
        }
        saveAllInternal(all);
    }

    private void saveAllInternal(List<AtividadeAluno> all) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), all);
            backupService.mirrorAfterWrite(dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar atividades", e);
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
            throw new IllegalStateException("Erro ao preparar atividades.json", e);
        }
    }
}
