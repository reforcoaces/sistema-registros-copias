package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.SaidaControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaidaControleRepository extends JpaRepository<SaidaControle, String> {

    @Override
    @Query("SELECT s FROM SaidaControle s ORDER BY s.dataHoraRegistro DESC")
    List<SaidaControle> findAll();
}
