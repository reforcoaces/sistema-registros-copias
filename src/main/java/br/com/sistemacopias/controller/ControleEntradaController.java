package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.EntradaControleForm;
import br.com.sistemacopias.model.MeioPagamentoEntrada;
import br.com.sistemacopias.model.TipoEntradaControle;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.service.ControleFluxoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String listar(Model model) {
        model.addAttribute("entradas", controleFluxoService.listarEntradasManuais());
        Map<String, String> nomes = alunoRepository.findAll().stream()
                .collect(Collectors.toMap(Aluno::getId, Aluno::getNomeCompleto, (a, b) -> a));
        model.addAttribute("nomesAlunos", nomes);
        return "controle-fluxo/entradas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararForm(model, new EntradaControleForm(), false);
        return "controle-fluxo/entrada-form";
    }

    @PostMapping("/nova")
    public String criar(@Valid @ModelAttribute("form") EntradaControleForm form, BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                prepararForm(model, form, false);
                return "controle-fluxo/entrada-form";
            }
            controleFluxoService.criarEntrada(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("entrada.invalida", ex.getMessage());
            prepararForm(model, form, false);
            return "controle-fluxo/entrada-form";
        }
        return "redirect:/controle-fluxo/entradas?sucesso=1";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        return controleFluxoService.buscarEntrada(id)
                .map(e -> {
                    prepararForm(model, controleFluxoService.paraFormEntrada(e), true);
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
        try {
            if (bindingResult.hasErrors()) {
                prepararForm(model, form, true);
                return "controle-fluxo/entrada-form";
            }
            controleFluxoService.atualizarEntrada(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("entrada.invalida", ex.getMessage());
            prepararForm(model, form, true);
            return "controle-fluxo/entrada-form";
        }
        return "redirect:/controle-fluxo/entradas?sucesso=1";
    }

    private void prepararForm(Model model, EntradaControleForm form, boolean editMode) {
        model.addAttribute("form", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("tiposEntrada", TipoEntradaControle.values());
        model.addAttribute("meiosPagamentoEntrada", MeioPagamentoEntrada.values());
        model.addAttribute("alunos", alunoRepository.findAll());
    }
}
