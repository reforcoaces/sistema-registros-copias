package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.BibliotecaEmprestimoForm;
import br.com.sistemacopias.model.Aluno;
import br.com.sistemacopias.repository.AlunoRepository;
import br.com.sistemacopias.service.BibliotecaEmprestimoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/biblioteca")
public class BibliotecaController {

    private final BibliotecaEmprestimoService emprestimoService;
    private final AlunoRepository alunoRepository;

    public BibliotecaController(BibliotecaEmprestimoService emprestimoService, AlunoRepository alunoRepository) {
        this.emprestimoService = emprestimoService;
        this.alunoRepository = alunoRepository;
    }

    @ModelAttribute("alunos")
    public List<Aluno> alunosCadastrados() {
        return alunoRepository.findAll();
    }

    @GetMapping({"", "/"})
    public String lista(Model model) {
        model.addAttribute("emprestimos", emprestimoService.listar());
        model.addAttribute("hoje", LocalDate.now());
        return "biblioteca/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("emprestimoForm")) {
            BibliotecaEmprestimoForm f = new BibliotecaEmprestimoForm();
            f.setDataEmprestimo(LocalDate.now());
            model.addAttribute("emprestimoForm", f);
        }
        return "biblioteca/novo";
    }

    @PostMapping("/novo")
    public String criar(
            @Valid @ModelAttribute("emprestimoForm") BibliotecaEmprestimoForm form,
            BindingResult bindingResult) {
        var alunoOpt = emprestimoService.resolverAluno(form);
        if (alunoOpt.isEmpty() && !bindingResult.hasFieldErrors("alunoId")) {
            bindingResult.rejectValue("alunoId", "alunoId.invalido", "Selecione um aluno valido da lista.");
        }
        if (bindingResult.hasErrors()) {
            return "biblioteca/novo";
        }
        emprestimoService.registrar(form, alunoOpt.get());
        return "redirect:/biblioteca?registado=1";
    }
}
