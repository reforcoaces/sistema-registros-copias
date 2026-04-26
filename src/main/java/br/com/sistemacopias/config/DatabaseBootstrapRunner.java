package br.com.sistemacopias.config;

import br.com.sistemacopias.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Criacao de utilizadores por defeito quando a base ainda nao tem nenhum.
 */
@Component
@Order(100)
public class DatabaseBootstrapRunner implements ApplicationRunner {

    private final UserService userService;

    public DatabaseBootstrapRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.ensureDefaultUsersIfEmpty();
    }
}
