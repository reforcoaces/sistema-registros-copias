package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.EntradaControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntradaControleRepository extends JpaRepository<EntradaControle, String> {

    @Override
    @Query("SELECT e FROM EntradaControle e ORDER BY e.dataHoraRegistro DESC")
    List<EntradaControle> findAll();
}
