package br.com.sistemacopias.service;

import br.com.sistemacopias.dto.AlunoForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.model.RecorrenciaMensalidade;
import br.com.sistemacopias.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReforcoAlunoService {
    private final AlunoRepository alunoRepository;
    private final ControleFluxoService controleFluxoService;

    public ReforcoAlunoService(AlunoRepository alunoRepository, ControleFluxoService controleFluxoService) {
        this.alunoRepository = alunoRepository;
        this.controleFluxoService = controleFluxoService;
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
                form.getTipoPcd()
        );
        aplicarCamposOpcionais(a, form);
        aplicarMensalidade(a, form, false);
        alunoRepository.save(a);
        controleFluxoService.sincronizarMensalidadeAposSalvarAluno(a.getId());
    }

    public void atualizar(AlunoForm form) {
        Aluno existente = alunoRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        existente.setNomeCompleto(form.getNomeCompleto().trim());
        existente.setIdade(form.getIdade() != null ? form.getIdade() : existente.getIdade());
        existente.setEscolaridade(form.getEscolaridade());
        existente.setTipoPcd(form.getTipoPcd());
        aplicarCamposOpcionais(existente, form);
        aplicarMensalidade(existente, form, true);
        alunoRepository.update(existente);
        controleFluxoService.sincronizarMensalidadeAposSalvarAluno(existente.getId());
    }

    public AlunoForm paraForm(Aluno a) {
        AlunoForm f = new AlunoForm();
        f.setId(a.getId());
        f.setNomeCompleto(a.getNomeCompleto());
        f.setIdade(Integer.valueOf(a.getIdade()));
        f.setEscolaridade(a.getEscolaridade());
        f.setTipoPcd(a.getTipoPcd());
        f.setSexo(a.getSexo());
        f.setNomePai(a.getNomePai());
        f.setNomeMae(a.getNomeMae());
        f.setTelefoneContato(a.getTelefoneContato());
        f.setObjetivoReforco(a.getObjetivoReforco());
        f.setExpectativaPais(a.getExpectativaPais());
        f.setValorMensalidade(a.getValorMensalidade());
        f.setDiaPagamentoPreferido(a.getDiaPagamentoPreferido());
        f.setRecorrenciaMensalidade(a.getRecorrenciaMensalidade());
        return f;
    }

    private static void aplicarMensalidade(Aluno a, AlunoForm form, boolean edicao) {
        if (form.getValorMensalidade() == null || form.getValorMensalidade().compareTo(BigDecimal.ZERO) <= 0) {
            a.setValorMensalidade(null);
            a.setDiaPagamentoPreferido(null);
            a.setRecorrenciaMensalidade(null);
            a.setProximaCobrancaPrevista(null);
            return;
        }
        a.setValorMensalidade(form.getValorMensalidade());
        a.setDiaPagamentoPreferido(form.getDiaPagamentoPreferido());
        RecorrenciaMensalidade rec = form.getRecorrenciaMensalidade() != null
                ? form.getRecorrenciaMensalidade()
                : RecorrenciaMensalidade.MENSAL;
        a.setRecorrenciaMensalidade(rec);
        if (!edicao || a.getProximaCobrancaPrevista() == null) {
            LocalDate reg = a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate() : LocalDate.now();
            a.setProximaCobrancaPrevista(ControleFluxoService.primeiraDataRecorrenteApos(reg, a.getDiaPagamentoPreferido(), rec));
        }
    }

    private static void aplicarCamposOpcionais(Aluno a, AlunoForm form) {
        a.setSexo(form.getSexo());
        a.setNomePai(trimToNull(form.getNomePai()));
        a.setNomeMae(trimToNull(form.getNomeMae()));
        a.setTelefoneContato(trimToNull(form.getTelefoneContato()));
        a.setObjetivoReforco(trimToNull(form.getObjetivoReforco()));
        a.setExpectativaPais(trimToNull(form.getExpectativaPais()));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
