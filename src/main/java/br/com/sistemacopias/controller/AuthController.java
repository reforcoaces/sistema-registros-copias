package br.com.sistemacopias.controller;

import br.com.sistemacopias.service.UserService;
import br.com.sistemacopias.support.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        return userService.authenticate(username, password)
                .map(user -> {
                    session.setAttribute("loggedUser", user);
                    session.removeAttribute(SessionKeys.SISTEMA_ATIVO);
                    return "redirect:/escolher-sistema";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Usuario ou senha invalidos");
                    return "login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
