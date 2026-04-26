package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.StatusAtividade;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class AtividadeEstadoForm {

    @NotNull(message = "Selecione o estado da atividade")
    private StatusAtividade status;

    /** Obrigatorio quando status e FINALIZADO (percepcao da professora Lucilene). */
    private String percepcaoProfessor;

    @AssertTrue(message = "Ao marcar como finalizada, registe a percepcao sobre o desenvolvimento do aluno.")
    public boolean isPercepcaoSeFinalizado() {
        if (status != StatusAtividade.FINALIZADO) {
            return true;
        }
        return percepcaoProfessor != null && !percepcaoProfessor.isBlank();
    }

    public StatusAtividade getStatus() {
        return status;
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
