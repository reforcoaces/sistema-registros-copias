package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false, unique = true, length = 120)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    public AppUser() {
    }

    public static AppUser create(String username, String passwordHash, UserRole role) {
        AppUser user = new AppUser();
        user.id = UUID.randomUUID().toString();
        user.username = username;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
