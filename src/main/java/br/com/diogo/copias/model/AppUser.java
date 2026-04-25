package br.com.sistemacopias.model;

import java.util.UUID;

public class AppUser {
    private String id;
    private String username;
    private String passwordHash;
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
