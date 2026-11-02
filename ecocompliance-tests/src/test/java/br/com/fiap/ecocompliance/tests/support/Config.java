package br.com.fiap.ecocompliance.tests.support;

public final class Config {
    public static final String ADMIN_EMAIL = "admin@ecocompliance.com";
    public static final String ADMIN_SENHA = "admin123";
    public static final String USER_EMAIL = "user@ecocompliance.com";
    public static final String USER_SENHA = "user123";

    private Config() {
    }

    public static String baseUrl() {
        String url = ler("api.baseUrl", "API_BASE_URL", "http://localhost:8080");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static int tempoEsperaApiSegundos() {
        return Integer.parseInt(ler("api.wait.timeoutSeconds", "API_WAIT_TIMEOUT_SECONDS", "180"));
    }

    private static String ler(String propriedade, String variavelAmbiente, String padrao) {
        String valor = System.getProperty(propriedade);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variavelAmbiente);
        }
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }
}
