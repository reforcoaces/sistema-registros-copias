package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.FluxoOrigemCompraCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FluxoOrigemCompraCustomJpaRepository extends JpaRepository<FluxoOrigemCompraCustom, Long> {

    Optional<FluxoOrigemCompraCustom> findByNomeIgnoreCase(String nome);

    List<FluxoOrigemCompraCustom> findAllByOrderByNomeAsc();
}
