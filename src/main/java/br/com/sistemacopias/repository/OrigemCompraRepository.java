package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.FluxoOrigemCompraCustom;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Origens de compra adicionadas pelo utilizador. As sugestoes fixas estao em {@link br.com.sistemacopias.support.OrigensCompraPadrao}.
 */
@Repository
public class OrigemCompraRepository {

    private final FluxoOrigemCompraCustomJpaRepository jpaRepository;

    public OrigemCompraRepository(FluxoOrigemCompraCustomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public List<String> findAllCustom() {
        List<String> list = new ArrayList<>();
        for (FluxoOrigemCompraCustom row : jpaRepository.findAllByOrderByNomeAsc()) {
            list.add(row.getNome());
        }
        list.sort(Comparator.comparing(String::toLowerCase));
        return list;
    }

    /**
     * Guarda na lista persistente se ainda nao existir (comparacao sem distinguir maiusculas).
     */
    public void addIfAbsentIgnoreCase(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String v = value.trim();
        if (jpaRepository.findByNomeIgnoreCase(v).isPresent()) {
            return;
        }
        jpaRepository.save(new FluxoOrigemCompraCustom(null, v));
    }
}
