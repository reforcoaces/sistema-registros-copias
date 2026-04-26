package br.com.sistemacopias.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Aluno {
    private String id;
    private String nomeCompleto;
    private int idade;
    private Escolaridade escolaridade;
    private boolean pcd;
    private LocalDateTime createdAt;

    public Aluno() {
    }

    public static Aluno novo(String nomeCompleto, int idade, Escolaridade escolaridade, boolean pcd) {
        Aluno a = new Aluno();
        a.id = UUID.randomUUID().toString();
        a.nomeCompleto = nomeCompleto;
        a.idade = idade;
        a.escolaridade = escolaridade;
        a.pcd = pcd;
        a.createdAt = LocalDateTime.now();
        return a;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public Escolaridade getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(Escolaridade escolaridade) {
        this.escolaridade = escolaridade;
    }

    public boolean isPcd() {
        return pcd;
    }

    public void setPcd(boolean pcd) {
        this.pcd = pcd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
