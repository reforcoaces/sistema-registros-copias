package br.com.sistemacopias.support;

import java.util.List;

public final class OrigensCompraPadrao {
    public static final List<String> TODAS = List.of(
            "Mercado Livre",
            "Shopee",
            "Amazon",
            "Magazine Luiza",
            "Americanas",
            "AliExpress",
            "Shein",
            "TikTok Shop",
            "Instagram / Facebook",
            "Loja fisica",
            "Site da marca",
            "Outro app ou site");

    private OrigensCompraPadrao() {
    }

    public static boolean contemIgnorandoMaiusculas(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        for (String p : TODAS) {
            if (p.equalsIgnoreCase(valor.trim())) {
                return true;
            }
        }
        return false;
    }
}
