package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.AlunoForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReforcoAlunoService {
    private final AlunoRepository alunoRepository;

    public ReforcoAlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    public Optional<Aluno> buscar(String id) {
        return alunoRepository.findById(id);
    }

    public void criar(AlunoForm form) {
        Aluno a = Aluno.novo(
                form.getNomeCompleto().trim(),
                form.getIdade() != null ? form.getIdade() : 0,
                form.getEscolaridade(),
                form.isPcd()
        );
        alunoRepository.save(a);
    }

    public void atualizar(AlunoForm form) {
        Aluno existente = alunoRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        existente.setNomeCompleto(form.getNomeCompleto().trim());
        existente.setIdade(form.getIdade() != null ? form.getIdade() : existente.getIdade());
        existente.setEscolaridade(form.getEscolaridade());
        existente.setPcd(form.isPcd());
        alunoRepository.update(existente);
    }

    public AlunoForm paraForm(Aluno a) {
        AlunoForm f = new AlunoForm();
        f.setId(a.getId());
        f.setNomeCompleto(a.getNomeCompleto());
        f.setIdade(Integer.valueOf(a.getIdade()));
        f.setEscolaridade(a.getEscolaridade());
        f.setPcd(a.isPcd());
        return f;
    }
}
