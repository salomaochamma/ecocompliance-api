package br.com.fiap.ecocompliance.tests.steps;

import br.com.fiap.ecocompliance.tests.support.Autenticacao;
import br.com.fiap.ecocompliance.tests.support.ClienteApi;
import br.com.fiap.ecocompliance.tests.support.ContextoTeste;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class AutenticacaoSteps {
    private final ContextoTeste ctx;

    public AutenticacaoSteps(ContextoTeste ctx) {
        this.ctx = ctx;
    }

    @Dado("que estou autenticado como {string}")
    public void queEstouAutenticadoComo(String perfil) {
        ctx.setToken(Autenticacao.tokenDoPerfil(perfil));
    }

    @Dado("que não estou autenticado")
    public void queNaoEstouAutenticado() {
        ctx.setToken(null);
    }

    @Dado("que estou usando um token inválido")
    public void queEstouUsandoUmTokenInvalido() {
        ctx.setToken("token.invalido.qa");
    }

    @Dado("que estou usando um token forjado para o e-mail {string}")
    public void queEstouUsandoUmTokenForjado(String email) {
        String[] partes = Autenticacao.tokenDoPerfil("USER").split("\\.");
        String payloadForjado = "{\"sub\":\"" + ctx.resolver(email) + "\"}";
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadForjado.getBytes(StandardCharsets.UTF_8));
        ctx.setToken(partes[0] + "." + payloadBase64 + "." + partes[2]);
    }

    @Dado("que existe um usuário cadastrado com o e-mail {string}, senha {string} e perfil {string}")
    public void queExisteUmUsuarioCadastrado(String email, String senha, String perfil) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", "Usuário de Teste QA");
        corpo.put("email", ctx.resolver(email));
        corpo.put("senha", ctx.resolver(senha));
        corpo.put("role", perfil);

        Response resposta = ClienteApi.enviarSemToken(ctx, "POST", "/auth/register", corpo);
        if (resposta.getStatusCode() != 201) {
            throw new IllegalStateException("Pré-condição falhou: cadastro de usuário retornou HTTP "
                    + resposta.getStatusCode() + " - " + resposta.asString());
        }
    }

    @Quando("faço login com o e-mail {string} e a senha {string}")
    public void facoLogin(String email, String senha) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("email", ctx.resolver(email));
        corpo.put("senha", ctx.resolver(senha));
        ctx.setUltimaResposta(ClienteApi.enviarSemToken(ctx, "POST", "/auth/login", corpo));
    }
}
