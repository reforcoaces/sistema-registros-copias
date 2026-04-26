package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.ReforcoAgendaSingleton;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReforcoAgendaSingletonRepository extends JpaRepository<ReforcoAgendaSingleton, String> {
}
