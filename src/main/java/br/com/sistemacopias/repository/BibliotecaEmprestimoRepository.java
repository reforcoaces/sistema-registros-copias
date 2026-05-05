package br.com.sistemacopias.repository;

import br.com.sistemacopias.model.BibliotecaEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BibliotecaEmprestimoRepository extends JpaRepository<BibliotecaEmprestimo, String> {

    @Query("SELECT e FROM BibliotecaEmprestimo e ORDER BY e.dataEmprestimo DESC, e.createdAt DESC")
    List<BibliotecaEmprestimo> listarOrdenados();
}
