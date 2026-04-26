package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.ReforcoDashboardView;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.model.AtividadeAluno;
import br.com.sistemacopias.repository.AgendaRepository;
import br.com.sistemacopias.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReforcoDashboardService {
    private final AlunoRepository alunoRepository;
    private final ReforcoAtividadeService atividadeService;
    private final AgendaRepository agendaRepository;

    public ReforcoDashboardService(
            AlunoRepository alunoRepository,
            ReforcoAtividadeService atividadeService,
            AgendaRepository agendaRepository) {
        this.alunoRepository = alunoRepository;
        this.atividadeService = atividadeService;
        this.agendaRepository = agendaRepository;
    }

    public ReforcoDashboardView montar() {
        ReforcoDashboardView v = new ReforcoDashboardView();
        var alunos = alunoRepository.findAll();
        v.setTotalAlunos(alunos.size());
        v.setAlunosPcd((int) alunos.stream().filter(Aluno::isPcd).count());

        LocalDateTime limite = LocalDateTime.now().minusDays(7);
        List<AtividadeAluno> todas = atividadeService.todasOrdenadasDesc();
        int ult7 = (int) todas.stream().filter(a -> a.getCreatedAt().isAfter(limite)).count();
        v.setAtividadesUltimos7Dias(ult7);
        v.setUltimasAtividades(todas.stream().limit(12).toList());
        v.setAgenda(agendaRepository.load());
        Map<String, String> nomes = alunos.stream()
                .collect(Collectors.toMap(Aluno::getId, Aluno::getNomeCompleto, (a, b) -> a));
        v.setNomeAlunoPorId(nomes);
        Map<String, Integer> porEsc = alunos.stream()
                .filter(a -> a.getEscolaridade() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getEscolaridade().getLabel(),
                        LinkedHashMap::new,
                        Collectors.summingInt(x -> 1)));
        v.setAlunosPorEscolaridade(porEsc);
        return v;
    }
}
