package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.OrderRecord;
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
public class OrderRepository {
    private final Path dataFilePath;
    private final ObjectMapper objectMapper;
    private final DataFileBackupService backupService;

    public OrderRepository(
            @Value("${app.storage.path:data/orders.json}") String storagePath,
            DataFileBackupService backupService) {
        this.dataFilePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.backupService = backupService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized List<OrderRecord> findAll() {
        ensureFileExists();
        try {
            byte[] bytes = Files.readAllBytes(dataFilePath);
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            List<OrderRecord> orders = objectMapper.readValue(bytes, new TypeReference<>() {
            });
            orders.sort(Comparator.comparing(OrderRecord::getCreatedAt).reversed());
            return orders;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler arquivo de pedidos", e);
        }
    }

    public synchronized void save(OrderRecord newOrder) {
        List<OrderRecord> allOrders = findAll();
        allOrders.add(newOrder);
        saveAllInternal(allOrders);
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public synchronized Optional<OrderRecord> findById(String id) {
        return findAll().stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    public synchronized void update(OrderRecord updatedOrder) {
        List<OrderRecord> all = findAll();
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(updatedOrder.getId())) {
                all.set(i, updatedOrder);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            throw new IllegalArgumentException("Pedido nao encontrado");
        }
        saveAllInternal(all);
    }

    private void saveAllInternal(List<OrderRecord> allOrders) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFilePath.toFile(), allOrders);
            backupService.mirrorAfterWrite(dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar pedido", e);
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
            throw new IllegalStateException("Erro ao preparar arquivo de dados", e);
        }
    }
}
