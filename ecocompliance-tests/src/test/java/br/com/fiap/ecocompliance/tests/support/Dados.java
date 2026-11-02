package br.com.fiap.ecocompliance.tests.support;

import java.security.SecureRandom;
import java.util.Locale;

public final class Dados {
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String ALFANUMERICOS = "abcdefghijklmnopqrstuvwxyz0123456789";

    private Dados() {
    }

    public static String sufixo(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(ALFANUMERICOS.charAt(ALEATORIO.nextInt(ALFANUMERICOS.length())));
        }
        return sb.toString();
    }

    public static String cnpjUnico() {
        StringBuilder digitos = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            digitos.append(ALEATORIO.nextInt(10));
        }
        String d = digitos.toString();
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8)
                + "/" + d.substring(8, 12) + "-" + d.substring(12, 14);
    }

    public static String emailUnico() {
        return "qa." + sufixo(10) + "@ecocompliance-qa.com";
    }

    public static String numeroLicencaUnico() {
        return "QA-" + sufixo(12).toUpperCase(Locale.ROOT);
    }

    public static String nomeEmpresaUnico() {
        return "Empresa QA " + sufixo(6).toUpperCase(Locale.ROOT);
    }
}
