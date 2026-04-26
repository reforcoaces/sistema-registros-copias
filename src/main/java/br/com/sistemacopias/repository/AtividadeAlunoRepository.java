package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.AtividadeAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtividadeAlunoRepository extends JpaRepository<AtividadeAluno, String> {

    List<AtividadeAluno> findByAlunoIdOrderByCreatedAtDesc(String alunoId);

    List<AtividadeAluno> findAllByOrderByCreatedAtDesc();
}
