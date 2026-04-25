package br.com.sistemacopias.config;

import br.com.sistemacopias.model.AppUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    @ModelAttribute("currentUser")
    public AppUser currentUser(HttpSession session) {
        return (AppUser) session.getAttribute("loggedUser");
    }
}
