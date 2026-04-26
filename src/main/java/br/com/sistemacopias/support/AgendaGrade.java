package br.com.sistemacopias.support;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Grade fixa: segunda a sexta, 8h as 18h, aulas de 1h15 (sem sobreposicao).
 */
public final class AgendaGrade {

    public record CelulaDef(String id, String diaLabel, String codigoDia, LocalTime inicio, LocalTime fim) {
        public String horarioLabel() {
            return String.format("%02d:%02d - %02d:%02d", inicio.getHour(), inicio.getMinute(),
                    fim.getHour(), fim.getMinute());
        }
    }

    private static final List<CelulaDef> DEFS;

    static {
        String[][] dias = {
                {"SEG", "Segunda"},
                {"TER", "Terca-feira"},
                {"QUA", "Quarta"},
                {"QUI", "Quinta"},
                {"SEX", "Sexta"},
        };
        List<LocalTime> inicios = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        while (true) {
            LocalTime fim = t.plusMinutes(75);
            if (fim.isAfter(LocalTime.of(18, 0))) {
                break;
            }
            inicios.add(t);
            t = fim;
        }
        ArrayList<CelulaDef> list = new ArrayList<>();
        for (String[] dia : dias) {
            for (LocalTime ini : inicios) {
                LocalTime f = ini.plusMinutes(75);
                String id = dia[0] + "_" + String.format("%02d%02d", ini.getHour(), ini.getMinute());
                list.add(new CelulaDef(id, dia[1], dia[0], ini, f));
            }
        }
        DEFS = Collections.unmodifiableList(list);
    }

    private AgendaGrade() {
    }

    public static List<CelulaDef> celulas() {
        return DEFS;
    }

    public static int slotsPorDia() {
        long n = DEFS.stream().map(CelulaDef::codigoDia).distinct().count();
        return (int) (DEFS.size() / Math.max(1, n));
    }

    /**
     * Uma linha por horario (mesmo horario nas cinco feiras), cada linha com 5 celulas na ordem seg-sex.
     */
    public static List<List<CelulaDef>> linhasPorHorario() {
        String[] cod = {"SEG", "TER", "QUA", "QUI", "SEX"};
        List<LocalTime> tempos = DEFS.stream()
                .filter(c -> "SEG".equals(c.codigoDia()))
                .map(CelulaDef::inicio)
                .toList();
        List<List<CelulaDef>> rows = new ArrayList<>();
        for (LocalTime t : tempos) {
            List<CelulaDef> row = new ArrayList<>();
            for (String d : cod) {
                DEFS.stream()
                        .filter(c -> d.equals(c.codigoDia()) && c.inicio().equals(t))
                        .findFirst()
                        .ifPresent(row::add);
            }
            rows.add(row);
        }
        return rows;
    }
}
