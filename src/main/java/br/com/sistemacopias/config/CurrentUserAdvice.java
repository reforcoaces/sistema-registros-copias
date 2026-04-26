package br.com.sistemacopias.config;

import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.support.ReforcoAccess;
import br.com.sistemacopias.support.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    @ModelAttribute("currentUser")
    public AppUser currentUser(HttpSession session) {
        return (AppUser) session.getAttribute("loggedUser");
    }

    @ModelAttribute("podeReforco")
    public boolean podeReforco(HttpSession session) {
        return ReforcoAccess.podeAcessarReforco((AppUser) session.getAttribute("loggedUser"));
    }

    @ModelAttribute("sistemaAtivo")
    public String sistemaAtivo(HttpSession session) {
        Object v = session.getAttribute(SessionKeys.SISTEMA_ATIVO);
        return v == null ? "" : v.toString();
    }
}
