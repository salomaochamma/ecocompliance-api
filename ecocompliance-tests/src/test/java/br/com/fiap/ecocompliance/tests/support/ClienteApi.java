package br.com.fiap.ecocompliance.tests.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Locale;

public final class ClienteApi {
    private static final ObjectMapper JSON = new ObjectMapper();

    private ClienteApi() {
    }

    public static Response enviar(ContextoTeste ctx, String metodo, String caminho, Object corpo) {
        return enviar(ctx, metodo, caminho, corpo, ctx.getToken(), "Requisição");
    }

    public static Response enviarSemToken(ContextoTeste ctx, String metodo, String caminho, Object corpo) {
        return enviar(ctx, metodo, caminho, corpo, null, "Requisição");
    }

    public static Response enviarComoAdmin(ContextoTeste ctx, String metodo, String caminho, Object corpo) {
        return enviar(ctx, metodo, caminho, corpo, Autenticacao.tokenDoPerfil("ADMIN"), "Pré-condição");
    }

    public static void excluirEmpresaComoAdmin(Long id) {
        RestAssured.given()
                .header("Authorization", "Bearer " + Autenticacao.tokenDoPerfil("ADMIN"))
                .delete("/empresas/{id}", id);
    }

    private static Response enviar(ContextoTeste ctx, String metodo, String caminho, Object corpo,
                                   String token, String rotulo) {
        String verbo = metodo.trim().toUpperCase(Locale.ROOT);
        RequestSpecification requisicao = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        if (corpo instanceof String texto) {
            requisicao.body(texto);
        } else if (corpo != null) {
            requisicao.body(corpo);
        }

        Response resposta = requisicao.request(verbo, caminho);

        ctx.anexar(rotulo + ": " + verbo + " " + caminho, evidencia(verbo, caminho, corpo, token, resposta));
        return resposta;
    }

    private static String evidencia(String verbo, String caminho, Object corpo, String token, Response resposta) {
        StringBuilder sb = new StringBuilder();
        sb.append(">>> ").append(verbo).append(' ').append(RestAssured.baseURI).append(caminho).append('\n');
        sb.append("Authorization: ").append(token == null ? "(sem token)" : "Bearer ***").append('\n');
        if (corpo != null) {
            sb.append(formatar(corpo)).append('\n');
        }
        sb.append('\n');
        sb.append("<<< ").append(resposta.getStatusLine())
                .append("  (").append(resposta.getTime()).append(" ms)").append('\n');
        String location = resposta.getHeader("Location");
        if (location != null) {
            sb.append("Location: ").append(location).append('\n');
        }
        String corpoResposta = resposta.asString();
        sb.append(corpoResposta == null || corpoResposta.isBlank() ? "(sem corpo)" : formatar(corpoResposta));
        return sb.toString();
    }

    private static String formatar(Object conteudo) {
        try {
            Object arvore = conteudo instanceof String texto ? JSON.readTree(texto) : conteudo;
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(arvore);
        } catch (Exception e) {
            return String.valueOf(conteudo);
        }
    }
}
