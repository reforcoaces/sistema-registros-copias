package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderRecord, String> {

    @Override
    @Query("SELECT o FROM OrderRecord o ORDER BY o.createdAt DESC")
    List<OrderRecord> findAll();
}
