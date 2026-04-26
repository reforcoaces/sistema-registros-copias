package br.com.sistemacopias.service;

import br.com.sistemacopias.model.AtividadeAluno;
import br.com.sistemacopias.repository.AtividadeAlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReforcoAtividadeService {
    private final AtividadeAlunoRepository repository;

    public ReforcoAtividadeService(AtividadeAlunoRepository repository) {
        this.repository = repository;
    }

    public List<AtividadeAluno> listarPorAluno(String alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public void registrar(String alunoId, String texto) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Texto da atividade obrigatorio");
        }
        repository.save(AtividadeAluno.nova(alunoId, t));
    }

    public List<AtividadeAluno> todasOrdenadasDesc() {
        return repository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }
}
