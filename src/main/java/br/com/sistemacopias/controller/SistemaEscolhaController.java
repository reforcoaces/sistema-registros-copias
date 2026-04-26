package br.com.sistemacopias.controller;

import br.com.sistemacopias.support.ReforcoAccess;
import br.com.sistemacopias.support.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SistemaEscolhaController {

    @GetMapping("/escolher-sistema")
    public String escolher(HttpSession session, Model model,
                            @RequestParam(required = false) Boolean trocar) {
        if (Boolean.TRUE.equals(trocar)) {
            session.removeAttribute(SessionKeys.SISTEMA_ATIVO);
        }
        return "sistema-escolha";
    }

    @PostMapping("/escolher-sistema")
    public String definirSistema(@RequestParam("sistema") String sistema, HttpSession session) {
        if ("COPIAS".equalsIgnoreCase(sistema)) {
            session.setAttribute(SessionKeys.SISTEMA_ATIVO, SessionKeys.COPIAS);
            return "redirect:/";
        }
        if ("REFORCO".equalsIgnoreCase(sistema)) {
            var user = (br.com.sistemacopias.model.AppUser) session.getAttribute("loggedUser");
            if (!ReforcoAccess.podeAcessarReforco(user)) {
                return "redirect:/escolher-sistema?reforcoNegado=1";
            }
            session.setAttribute(SessionKeys.SISTEMA_ATIVO, SessionKeys.REFORCO);
            return "redirect:/reforco/dashboard";
        }
        return "redirect:/escolher-sistema";
    }
}
