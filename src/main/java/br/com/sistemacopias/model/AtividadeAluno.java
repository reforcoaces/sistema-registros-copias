package br.com.sistemacopias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reforco_atividade_aluno")
public class AtividadeAluno {
    public static final String PROFESSORA_PADRAO = "Lucilene Ramos";

    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "aluno_id", nullable = false, length = 36)
    private String alunoId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;
    @Column(length = 200)
    private String professora;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StatusAtividade status = StatusAtividade.FAZENDO;
    /** Percepcao da professora; obrigatorio quando {@link #status} e {@link StatusAtividade#FINALIZADO}. */
    @Column(name = "percepcao_professor", columnDefinition = "TEXT")
    private String percepcaoProfessor;

    public AtividadeAluno() {
    }

    @PostLoad
    private void aposCarregar() {
        migrarStatusLegadoSeNecessario();
    }

    public static AtividadeAluno nova(String alunoId, String texto, StatusAtividade status, String percepcaoProfessor) {
        AtividadeAluno a = new AtividadeAluno();
        a.id = UUID.randomUUID().toString();
        a.alunoId = alunoId;
        a.createdAt = LocalDateTime.now();
        a.texto = texto;
        a.professora = PROFESSORA_PADRAO;
        a.status = status != null ? status : StatusAtividade.A_FAZER;
        a.setPercepcaoProfessor(percepcaoProfessor);
        a.normalizarPercepcao();
        return a;
    }

    /** Registos antigos sem campo status no JSON. */
    public void migrarStatusLegadoSeNecessario() {
        if (status == null) {
            status = StatusAtividade.FAZENDO;
        }
        normalizarPercepcao();
    }

    public void normalizarPercepcao() {
        if (status != StatusAtividade.FINALIZADO) {
            percepcaoProfessor = null;
        } else if (percepcaoProfessor != null) {
            String t = percepcaoProfessor.trim();
            percepcaoProfessor = t.isEmpty() ? null : t;
        }
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    public StatusAtividade getStatus() {
        return status != null ? status : StatusAtividade.FAZENDO;
    }

    public void setStatus(StatusAtividade status) {
        this.status = status;
    }

    public String getPercepcaoProfessor() {
        return percepcaoProfessor;
    }

    public void setPercepcaoProfessor(String percepcaoProfessor) {
        this.percepcaoProfessor = percepcaoProfessor;
    }
}
