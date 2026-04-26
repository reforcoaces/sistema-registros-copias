package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.EntradaControleForm;
import br.com.sistemacopias.model.EntradaControle;
import br.com.sistemacopias.model.MeioPagamentoEntrada;
import br.com.sistemacopias.model.SituacaoEntrada;
import br.com.sistemacopias.model.TipoEntradaControle;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.service.ControleFluxoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/controle-fluxo/entradas")
public class ControleEntradaController {
    private final ControleFluxoService controleFluxoService;
    private final AlunoRepository alunoRepository;

    public ControleEntradaController(ControleFluxoService controleFluxoService, AlunoRepository alunoRepository) {
        this.controleFluxoService = controleFluxoService;
        this.alunoRepository = alunoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String alunoId,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model) {
        SituacaoEntrada sit = parseSituacao(situacao);
        var entradas = controleFluxoService.listarEntradasFiltradas(alunoId, sit, dataInicio, dataFim);
        Map<String, String> nomes = alunoRepository.findAll().stream()
                .collect(Collectors.toMap(Aluno::getId, Aluno::getNomeCompleto, (a, b) -> a));
        model.addAttribute("entradas", entradas);
        model.addAttribute("nomesAlunos", nomes);
        model.addAttribute("alunos", alunoRepository.findAll());
        model.addAttribute("filtroAlunoId", alunoId != null ? alunoId.trim() : "");
        model.addAttribute("filtroSituacao", situacao != null && !situacao.isBlank() ? situacao.trim() : "");
        model.addAttribute("filtroDataInicio", dataInicio != null ? dataInicio.toString() : "");
        model.addAttribute("filtroDataFim", dataFim != null ? dataFim.toString() : "");

        BigDecimal totalPeriodo = BigDecimal.ZERO;
        String aid = alunoId != null ? alunoId.trim() : "";
        if (!aid.isEmpty() && dataInicio != null && dataFim != null) {
            totalPeriodo = controleFluxoService.somarEntradasConfirmadasPagamentoAluno(aid, dataInicio, dataFim);
        }
        model.addAttribute("totalConfirmadoAlunoPeriodo", totalPeriodo);
        return "controle-fluxo/entradas";
    }

    private static SituacaoEntrada parseSituacao(String situacao) {
        if (situacao == null || situacao.isBlank()) {
            return null;
        }
        if ("pendente".equalsIgnoreCase(situacao.trim())) {
            return SituacaoEntrada.PENDENTE;
        }
        if ("confirmada".equalsIgnoreCase(situacao.trim())) {
            return SituacaoEntrada.CONFIRMADA;
        }
        return null;
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararForm(model, new EntradaControleForm(), false, false);
        return "controle-fluxo/entrada-form";
    }

    @PostMapping("/nova")
    public String criar(@Valid @ModelAttribute("form") EntradaControleForm form, BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                prepararForm(model, form, false, false);
                return "controle-fluxo/entrada-form";
            }
            controleFluxoService.criarEntrada(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("entrada.invalida", ex.getMessage());
            prepararForm(model, form, false, false);
            return "controle-fluxo/entrada-form";
        }
        return "redirect:/controle-fluxo/entradas?sucesso=1";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable String id) {
        try {
            controleFluxoService.confirmarEntrada(id);
        } catch (IllegalArgumentException ignored) {
            return "redirect:/controle-fluxo/entradas?erroConfirmar=1";
        }
        return "redirect:/controle-fluxo/entradas?confirmada=1";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        return controleFluxoService.buscarEntrada(id)
                .map(e -> {
                    boolean pendAuto = e.getSituacao() == SituacaoEntrada.PENDENTE && e.isEntradaAutomaticaMensalidade();
                    prepararForm(model, controleFluxoService.paraFormEntrada(e), true, pendAuto);
                    return "controle-fluxo/entrada-form";
                })
                .orElse("redirect:/controle-fluxo/entradas");
    }

    @PostMapping("/{id}/editar")
    public String editarSalvar(
            @PathVariable String id,
            @Valid @ModelAttribute("form") EntradaControleForm form,
            BindingResult bindingResult,
            Model model) {
        form.setId(id);
        boolean pendAuto = controleFluxoService.buscarEntrada(id)
                .map(e -> e.getSituacao() == SituacaoEntrada.PENDENTE && e.isEntradaAutomaticaMensalidade())
                .orElse(false);
        try {
            if (bindingResult.hasErrors()) {
                prepararForm(model, form, true, pendAuto);
                return "controle-fluxo/entrada-form";
            }
            controleFluxoService.atualizarEntrada(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("entrada.invalida", ex.getMessage());
            prepararForm(model, form, true, pendAuto);
            return "controle-fluxo/entrada-form";
        }
        return "redirect:/controle-fluxo/entradas?sucesso=1";
    }

    private void prepararForm(Model model, EntradaControleForm form, boolean editMode, boolean entradaPendenteAutomatica) {
        model.addAttribute("form", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("entradaPendenteAutomatica", entradaPendenteAutomatica);
        model.addAttribute("tiposEntrada", TipoEntradaControle.values());
        model.addAttribute("meiosPagamentoEntrada", MeioPagamentoEntrada.valoresFormularioManual());
        model.addAttribute("alunos", alunoRepository.findAll());
    }
}
