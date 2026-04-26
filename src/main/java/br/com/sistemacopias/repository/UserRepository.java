package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.AppUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final Path userFilePath;
    private final ObjectMapper objectMapper;

    public UserRepository(@Value("${app.users.path:data/users.json}") String usersPath) {
        this.userFilePath = Path.of(usersPath);
        this.objectMapper = new ObjectMapper();
    }

    public synchronized List<AppUser> findAll() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(userFilePath);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(bytes, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler usuarios", e);
        }
    }

    public synchronized Optional<AppUser> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public synchronized void saveAll(List<AppUser> users) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(userFilePath.toFile(), users);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar usuarios", e);
        }
    }

    private void ensureFileExists() {
        try {
            Path parent = userFilePath.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(userFilePath)) {
                Files.writeString(userFilePath, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao preparar arquivo de usuarios", e);
        }
    }
}
