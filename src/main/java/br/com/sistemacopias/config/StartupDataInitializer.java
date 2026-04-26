package br.com.sistemacopias.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class StartupDataInitializer implements CommandLineRunner {
    private final String storagePath;

    public StartupDataInitializer(@Value("${app.storage.path:data/orders.json}") String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void run(String... args) {
        Path path = Path.of(storagePath);
        Path parent = path.getParent();
        try {
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(path)) {
                Files.writeString(path, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel preparar o arquivo de dados", e);
        }
    }
}
