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
        ensureDefaults();
    }

    public Optional<AppUser> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> encoder.matches(password, user.getPasswordHash()));
    }

    public List<AppUser> listAll() {
        return userRepository.findAll();
    }

    public void createUser(String username, String password, UserRole role) {
        List<AppUser> users = new ArrayList<>(userRepository.findAll());
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            throw new IllegalArgumentException("Usuario ja existe");
        }
        users.add(AppUser.create(username, encoder.encode(password), role));
        userRepository.saveAll(users);
    }

    public void changePassword(String username, String newPassword) {
        List<AppUser> users = new ArrayList<>(userRepository.findAll());
        AppUser user = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.saveAll(users);
    }

    private void ensureDefaults() {
        List<AppUser> users = userRepository.findAll();
        if (!users.isEmpty()) {
            return;
        }
        users = new ArrayList<>();
        users.add(AppUser.create("diogo", encoder.encode("admin123"), UserRole.ADMIN));
        users.add(AppUser.create("lucilene", encoder.encode("colab123"), UserRole.COLABORADOR));
        users.add(AppUser.create("lukas", encoder.encode("colab123"), UserRole.COLABORADOR));
        userRepository.saveAll(users);
    }
}
