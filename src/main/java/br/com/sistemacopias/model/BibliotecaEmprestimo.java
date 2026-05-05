package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "biblioteca_emprestimo")
public class BibliotecaEmprestimo {

    @Id
    @Column(length = 36)
    private String id;

    /** Copia do nome no momento do emprestimo (registos antigos podem existir so com este campo). */
    @Column(name = "nome_aluno", nullable = false, length = 300)
    private String nomeAluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @Column(name = "titulo_livro", nullable = false, length = 500)
    private String tituloLivro;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BibliotecaEmprestimo() {
    }

    public static BibliotecaEmprestimo novo(
            Aluno aluno,
            String tituloLivro,
            LocalDate dataEmprestimo,
            LocalDate dataDevolucaoPrevista) {
        BibliotecaEmprestimo e = new BibliotecaEmprestimo();
        e.id = UUID.randomUUID().toString();
        e.aluno = aluno;
        e.nomeAluno = aluno.getNomeCompleto();
        e.tituloLivro = tituloLivro;
        e.dataEmprestimo = dataEmprestimo;
        e.dataDevolucaoPrevista = dataDevolucaoPrevista;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public String getId() {
        return id;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
