package br.com.fiap.ecocompliance.tests.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Autenticacao {
    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    private Autenticacao() {
    }

    public static String tokenDoPerfil(String perfil) {
        return TOKENS.computeIfAbsent(perfil.trim().toUpperCase(Locale.ROOT), Autenticacao::autenticar);
    }

    private static String autenticar(String perfil) {
        String[] credenciais = switch (perfil) {
            case "ADMIN" -> new String[]{Config.ADMIN_EMAIL, Config.ADMIN_SENHA};
            case "USER" -> new String[]{Config.USER_EMAIL, Config.USER_SENHA};
            default -> throw new IllegalArgumentException("Perfil desconhecido: " + perfil + " (use ADMIN ou USER)");
        };

        Response resposta = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", credenciais[0], "senha", credenciais[1]))
                .post("/auth/login");

        if (resposta.getStatusCode() != 200) {
            throw new IllegalStateException("Não foi possível autenticar como " + perfil
                    + " (HTTP " + resposta.getStatusCode() + "): " + resposta.asString());
        }
        return resposta.jsonPath().getString("token");
    }
}
