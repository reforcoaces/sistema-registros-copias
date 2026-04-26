package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.AlunoForm;
import br.com.sistemacopias.dto.AtividadeEstadoForm;
import br.com.sistemacopias.dto.AtividadeForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.model.Escolaridade;
import br.com.sistemacopias.model.RecorrenciaMensalidade;
import br.com.sistemacopias.model.Sexo;
import br.com.sistemacopias.model.StatusAtividade;
import br.com.sistemacopias.model.TipoPcd;
import br.com.sistemacopias.service.ControleFluxoService;
import br.com.sistemacopias.service.ReforcoAlunoService;
import br.com.sistemacopias.service.ReforcoAtividadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reforco/alunos")
public class ReforcoAlunoController {
    private static final List<TipoPcd> TIPOS_PCD_NOVO = List.of(TipoPcd.NAO, TipoPcd.TEA, TipoPcd.DOWN);

    private final ReforcoAlunoService alunoService;
    private final ReforcoAtividadeService atividadeService;
    private final ControleFluxoService controleFluxoService;

    public ReforcoAlunoController(
            ReforcoAlunoService alunoService,
            ReforcoAtividadeService atividadeService,
            ControleFluxoService controleFluxoService) {
        this.alunoService = alunoService;
        this.atividadeService = atividadeService;
        this.controleFluxoService = controleFluxoService;
    }

    private static List<TipoPcd> tiposPcdParaForm(boolean edicao) {
        return edicao ? Arrays.asList(TipoPcd.values()) : TIPOS_PCD_NOVO;
    }

    private void preencherModeloDetalhe(Model model, Aluno aluno) {
        model.addAttribute("aluno", aluno);
        model.addAttribute("atividades", atividadeService.listarPorAluno(aluno.getId()));
        model.addAttribute("atividadeForm", new AtividadeForm());
        model.addAttribute("statusesAtividade", StatusAtividade.values());
        LocalDate fim = LocalDate.now();
        LocalDate ini = fim.minusMonths(12);
        model.addAttribute("totalPagamentosAluno12Meses",
                controleFluxoService.somarEntradasConfirmadasPagamentoAluno(aluno.getId(), ini, fim));
        boolean temMen = aluno.getValorMensalidade() != null
                && aluno.getValorMensalidade().compareTo(BigDecimal.ZERO) > 0;
        model.addAttribute("alunoTemMensalidade", temMen);
    }

    private static void preencherModeloFormAluno(Model model) {
        model.addAttribute("recorrenciasMensalidade", RecorrenciaMensalidade.values());
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String filtroNome, Model model) {
        List<Aluno> lista = alunoService.listar();
        String q = filtroNome != null ? filtroNome.trim().toLowerCase(Locale.ROOT) : "";
        if (!q.isEmpty()) {
            lista = lista.stream()
                    .filter(a -> a.getNomeCompleto() != null
                            && a.getNomeCompleto().toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        model.addAttribute("alunos", lista);
        model.addAttribute("filtroNomeAluno", filtroNome != null ? filtroNome : "");
        return "reforco/alunos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("alunoForm", new AlunoForm());
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposPcd", tiposPcdParaForm(false));
        preencherModeloFormAluno(model);
        model.addAttribute("titulo", "Novo aluno");
        model.addAttribute("editMode", false);
        return "reforco/aluno-form";
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("alunoForm") AlunoForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("tiposPcd", tiposPcdParaForm(false));
            preencherModeloFormAluno(model);
            model.addAttribute("titulo", "Novo aluno");
            model.addAttribute("editMode", false);
            return "reforco/aluno-form";
        }
        alunoService.criar(form);
        return "redirect:/reforco/alunos?sucesso=1";
    }

    @GetMapping("/{id}/evolucao")
    public String evolucao(@PathVariable String id, Model model) {
        return alunoService.buscar(id)
                .map(aluno -> {
                    model.addAttribute("aluno", aluno);
                    model.addAttribute("evolucao", atividadeService.montarEvolucao(id));
                    model.addAttribute("atividades", atividadeService.listarPorAluno(id));
                    return "reforco/aluno-evolucao";
                })
                .orElse("redirect:/reforco/alunos");
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable String id, Model model) {
        return alunoService.buscar(id)
                .map(aluno -> {
                    preencherModeloDetalhe(model, aluno);
                    return "reforco/aluno-detalhe";
                })
                .orElse("redirect:/reforco/alunos");
    }

    @PostMapping("/{id}/atividades")
    public String registrarAtividade(
            @PathVariable String id,
            @Valid @ModelAttribute("atividadeForm") AtividadeForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return alunoService.buscar(id)
                    .map(aluno -> {
                        preencherModeloDetalhe(model, aluno);
                        model.addAttribute("atividadeForm", form);
                        return "reforco/aluno-detalhe";
                    })
                    .orElse("redirect:/reforco/alunos");
        }
        try {
            atividadeService.registrar(id, form);
        } catch (IllegalArgumentException ex) {
            return alunoService.buscar(id)
                    .map(aluno -> {
                        preencherModeloDetalhe(model, aluno);
                        model.addAttribute("erroAtividade", ex.getMessage());
                        return "reforco/aluno-detalhe";
                    })
                    .orElse("redirect:/reforco/alunos");
        }
        return "redirect:/reforco/alunos/" + id + "?atividadeOk=1";
    }

    @PostMapping("/{alunoId}/atividades/{atividadeId}/estado")
    public String atualizarEstadoAtividade(
            @PathVariable String alunoId,
            @PathVariable String atividadeId,
            @Valid @ModelAttribute("estadoForm") AtividadeEstadoForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return alunoService.buscar(alunoId)
                    .map(aluno -> {
                        preencherModeloDetalhe(model, aluno);
                        String msg = bindingResult.getAllErrors().stream()
                                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : err.getCode())
                                .collect(Collectors.joining(" "));
                        model.addAttribute("mensagemErroEstado", msg);
                        return "reforco/aluno-detalhe";
                    })
                    .orElse("redirect:/reforco/alunos");
        }
        try {
            atividadeService.atualizarEstado(alunoId, atividadeId, form);
        } catch (IllegalArgumentException ex) {
            return alunoService.buscar(alunoId)
                    .map(aluno -> {
                        preencherModeloDetalhe(model, aluno);
                        model.addAttribute("erroEstadoAtividade", ex.getMessage());
                        return "reforco/aluno-detalhe";
                    })
                    .orElse("redirect:/reforco/alunos");
        }
        return "redirect:/reforco/alunos/" + alunoId + "?estadoOk=1";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        return alunoService.buscar(id)
                .map(aluno -> {
                    model.addAttribute("alunoForm", alunoService.paraForm(aluno));
                    model.addAttribute("escolaridades", Escolaridade.values());
                    model.addAttribute("sexos", Sexo.values());
                    model.addAttribute("tiposPcd", tiposPcdParaForm(true));
                    preencherModeloFormAluno(model);
                    model.addAttribute("titulo", "Editar aluno");
                    model.addAttribute("editMode", true);
                    return "reforco/aluno-form";
                })
                .orElse("redirect:/reforco/alunos");
    }

    @PostMapping("/{id}/editar")
    public String editarSalvar(
            @PathVariable String id,
            @Valid @ModelAttribute("alunoForm") AlunoForm form,
            BindingResult bindingResult,
            Model model) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("tiposPcd", tiposPcdParaForm(true));
            preencherModeloFormAluno(model);
            model.addAttribute("titulo", "Editar aluno");
            model.addAttribute("editMode", true);
            return "reforco/aluno-form";
        }
        alunoService.atualizar(form);
        return "redirect:/reforco/alunos/" + id + "?sucesso=1";
    }
}
