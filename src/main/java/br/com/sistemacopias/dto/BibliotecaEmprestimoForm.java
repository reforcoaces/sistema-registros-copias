package br.com.sistemacopias.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BibliotecaEmprestimoForm {

    @NotBlank(message = "Selecione o aluno")
    @Size(max = 36, message = "Identificador do aluno invalido")
    private String alunoId;

    @NotBlank(message = "Informe o titulo do livro")
    @Size(max = 500, message = "Titulo do livro: no maximo 500 caracteres")
    private String tituloLivro;

    @NotNull(message = "Informe a data do emprestimo")
    private LocalDate dataEmprestimo;

    @NotNull(message = "Informe a data prevista de devolucao")
    private LocalDate dataDevolucaoPrevista;

    @AssertTrue(message = "A data de devolucao nao pode ser anterior a data do emprestimo")
    public boolean isDatasCoerentes() {
        if (dataEmprestimo == null || dataDevolucaoPrevista == null) {
            return true;
        }
        return !dataDevolucaoPrevista.isBefore(dataEmprestimo);
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public String getTituloLivro() {
        return tituloLivro;
    }

    public void setTituloLivro(String tituloLivro) {
        this.tituloLivro = tituloLivro;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }
}
