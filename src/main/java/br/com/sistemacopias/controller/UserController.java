package br.com.sistemacopias.controller;

import br.com.sistemacopias.model.UserRole;
import br.com.sistemacopias.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/usuarios")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.listAll());
        return "users";
    }

    @PostMapping("/usuarios/criar")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("role") UserRole role,
                             Model model) {
        try {
            userService.createUser(username, password, role);
            return "redirect:/usuarios?sucessoCriacao";
        } catch (Exception e) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("error", e.getMessage());
            return "users";
        }
    }

    @PostMapping("/usuarios/senha")
    public String changePassword(@RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 Model model) {
        try {
            userService.changePassword(username, password);
            return "redirect:/usuarios?sucessoSenha";
        } catch (Exception e) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("error", e.getMessage());
            return "users";
        }
    }
}
