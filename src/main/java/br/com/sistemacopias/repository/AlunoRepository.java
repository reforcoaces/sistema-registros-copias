package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, String> {

    @Override
    @Query("SELECT a FROM Aluno a ORDER BY LOWER(a.nomeCompleto)")
    List<Aluno> findAll();
}
