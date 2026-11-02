package br.com.fiap.ecocompliance.tests.support;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CorpoJson {
    public static final String VAZIO = "[vazio]";
    public static final String NULO = "[nulo]";

    private static final Set<String> CAMPOS_NUMERICOS = Set.of("empresaId", "quantidadeCo2", "quantidadeCompensada");

    private CorpoJson() {
    }

    public static Map<String, Object> montar(Map<String, String> tabela, ContextoTeste ctx) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        for (Map.Entry<String, String> linha : tabela.entrySet()) {
            corpo.put(linha.getKey(), converter(linha.getKey(), linha.getValue(), ctx));
        }
        return corpo;
    }

    static Object converter(String campo, String bruto, ContextoTeste ctx) {
        if (bruto == null || NULO.equals(bruto.trim())) {
            return null;
        }
        if (VAZIO.equals(bruto.trim())) {
            return "";
        }
        String valor = ctx.resolver(bruto.trim());
        if (CAMPOS_NUMERICOS.contains(campo)) {
            try {
                return valor.contains(".") ? new BigDecimal(valor) : Long.valueOf(valor);
            } catch (NumberFormatException e) {
                return valor;
            }
        }
        return valor;
    }
}
