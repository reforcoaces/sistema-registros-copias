package br.com.sistemacopias.dto;

import br.com.sistemacopias.model.AgendaSemana;
import br.com.sistemacopias.model.AtividadeAluno;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReforcoDashboardView {
    private int totalAlunos;
    private int alunosPcd;
    private int atividadesUltimos7Dias;
    private List<AtividadeAluno> ultimasAtividades;
    private AgendaSemana agenda;
    private Map<String, String> nomeAlunoPorId;
    /** Contagem por nivel escolar (label -> quantidade de alunos). */
    private Map<String, Integer> alunosPorEscolaridade = new LinkedHashMap<>();
    /** Texto de filtro por nome aplicado na listagem (pode ser vazio). */
    private String filtroNomeAluno = "";

    public String getFiltroNomeAluno() {
        return filtroNomeAluno;
    }

    public void setFiltroNomeAluno(String filtroNomeAluno) {
        this.filtroNomeAluno = filtroNomeAluno != null ? filtroNomeAluno : "";
    }

    public Map<String, String> getNomeAlunoPorId() {
        return nomeAlunoPorId;
    }

    public void setNomeAlunoPorId(Map<String, String> nomeAlunoPorId) {
        this.nomeAlunoPorId = nomeAlunoPorId;
    }

    public Map<String, Integer> getAlunosPorEscolaridade() {
        return alunosPorEscolaridade;
    }

    public void setAlunosPorEscolaridade(Map<String, Integer> alunosPorEscolaridade) {
        this.alunosPorEscolaridade = alunosPorEscolaridade != null ? alunosPorEscolaridade : new LinkedHashMap<>();
    }

    public int getTotalAlunos() {
        return totalAlunos;
    }

    public void setTotalAlunos(int totalAlunos) {
        this.totalAlunos = totalAlunos;
    }

    public int getAlunosPcd() {
        return alunosPcd;
    }

    public void setAlunosPcd(int alunosPcd) {
        this.alunosPcd = alunosPcd;
    }

    public int getAtividadesUltimos7Dias() {
        return atividadesUltimos7Dias;
    }

    public void setAtividadesUltimos7Dias(int atividadesUltimos7Dias) {
        this.atividadesUltimos7Dias = atividadesUltimos7Dias;
    }

    public List<AtividadeAluno> getUltimasAtividades() {
        return ultimasAtividades;
    }

    public void setUltimasAtividades(List<AtividadeAluno> ultimasAtividades) {
        this.ultimasAtividades = ultimasAtividades;
    }

    public AgendaSemana getAgenda() {
        return agenda;
    }

    public void setAgenda(AgendaSemana agenda) {
        this.agenda = agenda;
    }
}
