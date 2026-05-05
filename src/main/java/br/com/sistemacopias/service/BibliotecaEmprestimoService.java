package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.BibliotecaEmprestimoForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.model.BibliotecaEmprestimo;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.repository.BibliotecaEmprestimoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BibliotecaEmprestimoService {

    private final BibliotecaEmprestimoRepository repository;
    private final AlunoRepository alunoRepository;

    public BibliotecaEmprestimoService(BibliotecaEmprestimoRepository repository, AlunoRepository alunoRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
    }

    public List<BibliotecaEmprestimo> listar() {
        return repository.listarOrdenados();
    }

    /** Resolve o aluno do formulario; vazio se ID em branco ou inexistente. */
    public Optional<Aluno> resolverAluno(BibliotecaEmprestimoForm form) {
        if (form.getAlunoId() == null || form.getAlunoId().isBlank()) {
            return Optional.empty();
        }
        return alunoRepository.findById(form.getAlunoId().trim());
    }

    @Transactional
    public void registrar(BibliotecaEmprestimoForm form, Aluno aluno) {
        String titulo = form.getTituloLivro() == null ? "" : form.getTituloLivro().trim();
        BibliotecaEmprestimo row = BibliotecaEmprestimo.novo(
                aluno,
                titulo,
                form.getDataEmprestimo(),
                form.getDataDevolucaoPrevista());
        repository.save(row);
    }
}
