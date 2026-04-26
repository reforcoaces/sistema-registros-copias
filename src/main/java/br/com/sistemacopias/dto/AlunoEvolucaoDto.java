package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.AtividadeAluno;

import java.util.List;
import java.util.Map;

public class AlunoEvolucaoDto {
    private int totalAfazer;
    private int totalFazendo;
    private int totalFinalizado;
    private List<AtividadeAluno> ultimasFinalizadasComPercepcao;
    private Map<String, Long> contagemPorStatus;

    public int getTotalAfazer() {
        return totalAfazer;
    }

    public void setTotalAfazer(int totalAfazer) {
        this.totalAfazer = totalAfazer;
    }

    public int getTotalFazendo() {
        return totalFazendo;
    }

    public void setTotalFazendo(int totalFazendo) {
        this.totalFazendo = totalFazendo;
    }

    public int getTotalFinalizado() {
        return totalFinalizado;
    }

    public void setTotalFinalizado(int totalFinalizado) {
        this.totalFinalizado = totalFinalizado;
    }

    public List<AtividadeAluno> getUltimasFinalizadasComPercepcao() {
        return ultimasFinalizadasComPercepcao;
    }

    public void setUltimasFinalizadasComPercepcao(List<AtividadeAluno> ultimasFinalizadasComPercepcao) {
        this.ultimasFinalizadasComPercepcao = ultimasFinalizadasComPercepcao;
    }

    public Map<String, Long> getContagemPorStatus() {
        return contagemPorStatus;
    }

    public void setContagemPorStatus(Map<String, Long> contagemPorStatus) {
        this.contagemPorStatus = contagemPorStatus;
    }
}
