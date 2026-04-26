package br.com.sistemacopias.controller;

import br.com.sistemacopias.dto.AlunoForm;
import br.com.sistemacopias.dto.AtividadeForm;
import br.com.sistemacopias.model.Escolaridade;
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

@Controller
@RequestMapping("/reforco/alunos")
public class ReforcoAlunoController {
    private final ReforcoAlunoService alunoService;
    private final ReforcoAtividadeService atividadeService;

    public ReforcoAlunoController(ReforcoAlunoService alunoService, ReforcoAtividadeService atividadeService) {
        this.alunoService = alunoService;
        this.atividadeService = atividadeService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alunos", alunoService.listar());
        return "reforco/alunos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("alunoForm", new AlunoForm());
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("titulo", "Novo aluno");
        model.addAttribute("editMode", false);
        return "reforco/aluno-form";
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("alunoForm") AlunoForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("titulo", "Novo aluno");
            model.addAttribute("editMode", false);
            return "reforco/aluno-form";
        }
        alunoService.criar(form);
        return "redirect:/reforco/alunos?sucesso=1";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable String id, Model model) {
        return alunoService.buscar(id)
                .map(aluno -> {
                    model.addAttribute("aluno", aluno);
                    model.addAttribute("atividades", atividadeService.listarPorAluno(id));
                    model.addAttribute("atividadeForm", new AtividadeForm());
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
                        model.addAttribute("aluno", aluno);
                        model.addAttribute("atividades", atividadeService.listarPorAluno(id));
                        return "reforco/aluno-detalhe";
                    })
                    .orElse("redirect:/reforco/alunos");
        }
        atividadeService.registrar(id, form.getTexto());
        return "redirect:/reforco/alunos/" + id + "?atividadeOk=1";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        return alunoService.buscar(id)
                .map(aluno -> {
                    model.addAttribute("alunoForm", alunoService.paraForm(aluno));
                    model.addAttribute("escolaridades", Escolaridade.values());
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
            model.addAttribute("titulo", "Editar aluno");
            model.addAttribute("editMode", true);
            return "reforco/aluno-form";
        }
        alunoService.atualizar(form);
        return "redirect:/reforco/alunos/" + id + "?sucesso=1";
    }
}
