package br.com.sistemacopias.support;

import br.com.sistemacopias.model.AgendaHorarioConfig;
import br.com.sistemacopias.model.AgendaSemana;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Grade segunda a sexta: faixas consecutivas sem sobreposicao (por omissao 8h as 18h, blocos de 1h15).
 */
public final class AgendaGrade {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    public record CelulaDef(String id, String diaLabel, String codigoDia, LocalTime inicio, LocalTime fim) {
        public String horarioLabel() {
            return String.format("%02d:%02d - %02d:%02d", inicio.getHour(), inicio.getMinute(),
                    fim.getHour(), fim.getMinute());
        }
    }

    private static final List<CelulaDef> DEFS;

    static {
        DEFS = Collections.unmodifiableList(construirGrade(LocalTime.of(8, 0), 75, LocalTime.of(18, 0)));
    }

    private AgendaGrade() {
    }

    public static List<CelulaDef> celulas() {
        return DEFS;
    }

    /** Celulas efectivas conforme a configuracao guardada na agenda (ou padrao historico). */
    public static List<CelulaDef> celulasDaAgenda(AgendaSemana semana) {
        if (semana == null || semana.getHorarioConfig() == null || semana.getHorarioConfig().isPadraoHistorico()) {
            return DEFS;
        }
        AgendaHorarioConfig cfg = semana.getHorarioConfig();
        try {
            LocalTime ini = LocalTime.parse(cfg.getHoraInicio().trim(), FMT);
            LocalTime lim = LocalTime.parse(cfg.getHoraLimite().trim(), FMT);
            int dur = cfg.getDuracaoMinutos();
            if (dur < 15 || dur > 240) {
                return DEFS;
            }
            return Collections.unmodifiableList(construirGrade(ini, dur, lim));
        } catch (DateTimeParseException | NullPointerException e) {
            return DEFS;
        }
    }

    private static List<CelulaDef> construirGrade(LocalTime primeiraAula, int duracaoMinutos, LocalTime horaLimiteFim) {
        String[][] dias = {
                {"SEG", "Segunda"},
                {"TER", "Terca-feira"},
                {"QUA", "Quarta"},
                {"QUI", "Quinta"},
                {"SEX", "Sexta"},
        };
        List<LocalTime> inicios = new ArrayList<>();
        LocalTime t = primeiraAula;
        while (true) {
            LocalTime fim = t.plusMinutes(duracaoMinutos);
            if (fim.isAfter(horaLimiteFim)) {
                break;
            }
            inicios.add(t);
            t = fim;
        }
        ArrayList<CelulaDef> list = new ArrayList<>();
        for (String[] dia : dias) {
            for (LocalTime ini : inicios) {
                LocalTime f = ini.plusMinutes(duracaoMinutos);
                String id = dia[0] + "_" + String.format("%02d%02d", ini.getHour(), ini.getMinute());
                list.add(new CelulaDef(id, dia[1], dia[0], ini, f));
            }
        }
        return list;
    }

    public static int slotsPorDia() {
        long n = DEFS.stream().map(CelulaDef::codigoDia).distinct().count();
        return (int) (DEFS.size() / Math.max(1, n));
    }

    public static List<List<CelulaDef>> linhasPorHorario() {
        return linhasPorHorario(DEFS);
    }

    /**
     * Uma linha por horario (mesmo horario nas cinco feiras), cada linha com 5 celulas na ordem seg-sex.
     */
    public static List<List<CelulaDef>> linhasPorHorario(List<CelulaDef> defs) {
        String[] cod = {"SEG", "TER", "QUA", "QUI", "SEX"};
        List<LocalTime> tempos = defs.stream()
                .filter(c -> "SEG".equals(c.codigoDia()))
                .map(CelulaDef::inicio)
                .toList();
        List<List<CelulaDef>> rows = new ArrayList<>();
        for (LocalTime t : tempos) {
            List<CelulaDef> row = new ArrayList<>();
            for (String d : cod) {
                defs.stream()
                        .filter(c -> d.equals(c.codigoDia()) && c.inicio().equals(t))
                        .findFirst()
                        .ifPresent(row::add);
            }
            rows.add(row);
        }
        return rows;
    }
}
