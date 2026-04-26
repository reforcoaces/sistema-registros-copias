package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.AlunoEvolucaoDto;
import br.com.sistemacopias.dto.AtividadeEstadoForm;
import br.com.sistemacopias.dto.AtividadeForm;
import br.com.sistemacopias.model.AtividadeAluno;
import br.com.sistemacopias.model.StatusAtividade;
import br.com.sistemacopias.repository.AtividadeAlunoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReforcoAtividadeService {
    private final AtividadeAlunoRepository repository;

    public ReforcoAtividadeService(AtividadeAlunoRepository repository) {
        this.repository = repository;
    }

    public List<AtividadeAluno> listarPorAluno(String alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public Optional<AtividadeAluno> buscarPorId(String id) {
        return repository.findById(id);
    }

    public void registrar(String alunoId, AtividadeForm form) {
        String t = form.getTexto() == null ? "" : form.getTexto().trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Texto da atividade obrigatorio");
        }
        AtividadeAluno a = AtividadeAluno.nova(alunoId, t, form.getStatus(), form.getPercepcaoProfessor());
        repository.save(a);
    }

    public void atualizarEstado(String alunoId, String atividadeId, AtividadeEstadoForm form) {
        AtividadeAluno a = repository.findById(atividadeId)
                .orElseThrow(() -> new IllegalArgumentException("Atividade nao encontrada"));
        if (!a.getAlunoId().equals(alunoId)) {
            throw new IllegalArgumentException("Atividade nao pertence a este aluno");
        }
        a.setStatus(form.getStatus());
        String p = form.getPercepcaoProfessor() == null ? "" : form.getPercepcaoProfessor().trim();
        a.setPercepcaoProfessor(p.isEmpty() ? null : p);
        a.normalizarPercepcao();
        if (a.getStatus() == StatusAtividade.FINALIZADO
                && (a.getPercepcaoProfessor() == null || a.getPercepcaoProfessor().isBlank())) {
            throw new IllegalArgumentException("Registe a percepcao sobre o desenvolvimento ao finalizar.");
        }
        a.setUpdatedAt(LocalDateTime.now());
        repository.update(a);
    }

    public AlunoEvolucaoDto montarEvolucao(String alunoId) {
        List<AtividadeAluno> list = listarPorAluno(alunoId);
        AlunoEvolucaoDto dto = new AlunoEvolucaoDto();
        int af = 0;
        int fz = 0;
        int fn = 0;
        for (AtividadeAluno a : list) {
            StatusAtividade s = a.getStatus();
            if (s == StatusAtividade.A_FAZER) {
                af++;
            } else if (s == StatusAtividade.FAZENDO) {
                fz++;
            } else {
                fn++;
            }
        }
        dto.setTotalAfazer(af);
        dto.setTotalFazendo(fz);
        dto.setTotalFinalizado(fn);
        Map<String, Long> map = new LinkedHashMap<>();
        map.put(StatusAtividade.A_FAZER.getLabel(), (long) af);
        map.put(StatusAtividade.FAZENDO.getLabel(), (long) fz);
        map.put(StatusAtividade.FINALIZADO.getLabel(), (long) fn);
        dto.setContagemPorStatus(map);
        dto.setUltimasFinalizadasComPercepcao(
                list.stream()
                        .filter(a -> a.getStatus() == StatusAtividade.FINALIZADO
                                && a.getPercepcaoProfessor() != null
                                && !a.getPercepcaoProfessor().isBlank())
                        .limit(15)
                        .toList());
        return dto;
    }

    public List<AtividadeAluno> todasOrdenadasDesc() {
        return repository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }
}
