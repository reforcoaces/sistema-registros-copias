package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.SaidaControleForm;
import br.com.sistemacopias.model.CategoriaSaidaRecorrente;
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

@Controller
@RequestMapping("/controle-fluxo/saidas")
public class ControleSaidaController {
    private final ControleFluxoService controleFluxoService;

    public ControleSaidaController(ControleFluxoService controleFluxoService) {
        this.controleFluxoService = controleFluxoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("saidas", controleFluxoService.listarSaidas());
        return "controle-fluxo/saidas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("form", new SaidaControleForm());
        prepararFormSaida(model, false);
        return "controle-fluxo/saida-form";
    }

    @PostMapping("/nova")
    public String criar(@Valid @ModelAttribute("form") SaidaControleForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepararFormSaida(model, false);
            return "controle-fluxo/saida-form";
        }
        controleFluxoService.criarSaida(form);
        return "redirect:/controle-fluxo/saidas?sucesso=1";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        return controleFluxoService.buscarSaida(id)
                .map(s -> {
                    model.addAttribute("form", controleFluxoService.paraFormSaida(s));
                    prepararFormSaida(model, true);
                    return "controle-fluxo/saida-form";
                })
                .orElse("redirect:/controle-fluxo/saidas");
    }

    @PostMapping("/{id}/editar")
    public String editarSalvar(
            @PathVariable String id,
            @Valid @ModelAttribute("form") SaidaControleForm form,
            BindingResult bindingResult,
            Model model) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            prepararFormSaida(model, true);
            return "controle-fluxo/saida-form";
        }
        controleFluxoService.atualizarSaida(form);
        return "redirect:/controle-fluxo/saidas?sucesso=1";
    }

    private void prepararFormSaida(Model model, boolean editMode) {
        model.addAttribute("categoriasSaida", CategoriaSaidaRecorrente.values());
        model.addAttribute("editMode", editMode);
        model.addAttribute("origensCompra", controleFluxoService.listarOrigensCompraParaUi());
    }
}
