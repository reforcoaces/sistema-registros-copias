package br.com.sistemacopias.service;

import br.com.sistemacopias.model.AppUser;
import br.com.sistemacopias.model.UserRole;
import br.com.sistemacopias.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AppUser> authenticate(String username, String password) {
        return userRepository.findByUsernameIgnoreCase(username)
                .filter(user -> encoder.matches(password, user.getPasswordHash()));
    }

    public List<AppUser> listAll() {
        return userRepository.findAll();
    }

    public void createUser(String username, String password, UserRole role) {
        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Usuario ja existe");
        }
        userRepository.save(AppUser.create(username, encoder.encode(password), role));
    }

    public void changePassword(String username, String newPassword) {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Chamado apos o arranque da aplicacao se ainda nao existir nenhum utilizador na base.
     */
    public void ensureDefaultUsersIfEmpty() {
        List<AppUser> users = userRepository.findAll();
        if (!users.isEmpty()) {
            return;
        }
        users = new ArrayList<>();
        users.add(AppUser.create("admin", encoder.encode("admin123"), UserRole.ADMIN));
        users.add(AppUser.create("lucilene", encoder.encode("colab123"), UserRole.COLABORADOR));
        users.add(AppUser.create("lukas", encoder.encode("colab123"), UserRole.COLABORADOR));
        userRepository.saveAllAndFlush(users);
    }
}
