package br.com.sistemacopias.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copia opcional de ficheiros JSON para uma pasta externa logo apos cada gravacao.
 * Ative com {@code app.backup.folder} ou variavel de ambiente {@code APP_BACKUP_FOLDER}.
 */
@Component
public class DataFileBackupService {
    private static final Logger log = LoggerFactory.getLogger(DataFileBackupService.class);
    private final Path backupDir;

    public DataFileBackupService(@Value("${app.backup.folder:}") String folder) {
        if (folder == null || folder.isBlank()) {
            this.backupDir = null;
        } else {
            this.backupDir = Path.of(folder.trim()).toAbsolutePath().normalize();
        }
    }

    public void mirrorAfterWrite(Path writtenFile) {
        if (backupDir == null) {
            return;
        }
        try {
            Path src = writtenFile.toAbsolutePath().normalize();
            if (!Files.isRegularFile(src)) {
                return;
            }
            Files.createDirectories(backupDir);
            Path dest = backupDir.resolve(src.getFileName().toString());
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Backup para {} falhou ({}): {}", backupDir, writtenFile, e.getMessage());
        }
    }
}
