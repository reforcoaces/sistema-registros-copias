package br.com.sistemacopias.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AtividadeAluno {
    public static final String PROFESSORA_PADRAO = "Lucilene Ramos";

    private String id;
    private String alunoId;
    private LocalDateTime createdAt;
    private String texto;
    private String professora;

    public AtividadeAluno() {
    }

    public static AtividadeAluno nova(String alunoId, String texto) {
        AtividadeAluno a = new AtividadeAluno();
        a.id = UUID.randomUUID().toString();
        a.alunoId = alunoId;
        a.createdAt = LocalDateTime.now();
        a.texto = texto;
        a.professora = PROFESSORA_PADRAO;
        return a;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getProfessora() {
        return professora;
    }

    public void setProfessora(String professora) {
        this.professora = professora;
    }
}
