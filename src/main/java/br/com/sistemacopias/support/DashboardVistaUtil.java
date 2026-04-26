package br.com.sistemacopias.support;

public final class DashboardVistaUtil {
    private DashboardVistaUtil() {
    }

    public static String copias(String vistaExtra) {
        return oneOf(vistaExtra, "nenhuma", "pizza_pagamento", "barras_produto");
    }

    public static String fluxo(String vistaExtra) {
        return oneOf(vistaExtra, "nenhuma", "pizza_resumo", "pizza_entradas", "barras_resumo");
    }

    public static String reforco(String vistaExtra) {
        return oneOf(vistaExtra, "nenhuma", "pizza_escolaridade", "barras_pcd");
    }

    private static String oneOf(String vistaExtra, String def, String... allowed) {
        if (vistaExtra == null || vistaExtra.isBlank()) {
            return def;
        }
        for (String a : allowed) {
            if (a.equals(vistaExtra)) {
                return vistaExtra;
            }
        }
        return def;
    }
}
