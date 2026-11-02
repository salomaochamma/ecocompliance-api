package br.com.fiap.ecocompliance.tests.steps;

import br.com.fiap.ecocompliance.tests.support.ClienteApi;
import br.com.fiap.ecocompliance.tests.support.ContextoTeste;
import br.com.fiap.ecocompliance.tests.support.CorpoJson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

public class RequisicaoSteps {
    private final ContextoTeste ctx;

    public RequisicaoSteps(ContextoTeste ctx) {
        this.ctx = ctx;
    }

    @Quando("envio uma requisição {word} para {string}")
    public void envioUmaRequisicao(String metodo, String caminho) {
        executar(metodo, caminho, null);
    }

    @Quando("envio uma requisição {word} para {string} com o corpo:")
    public void envioUmaRequisicaoComCorpo(String metodo, String caminho, DataTable tabela) {
        executar(metodo, caminho, CorpoJson.montar(tabela.asMap(String.class, String.class), ctx));
    }

    @Quando("envio uma requisição {word} para {string} com o corpo bruto:")
    public void envioUmaRequisicaoComCorpoBruto(String metodo, String caminho, String corpo) {
        executar(metodo, caminho, corpo);
    }

    @E("guardo o campo {string} da resposta como {string}")
    public void guardoOCampoDaResposta(String campo, String nome) {
        Object valor = ctx.resposta().jsonPath().get(campo);
        if (valor == null) {
            throw new AssertionError("O campo '" + campo + "' não existe na resposta: " + ctx.resposta().asString());
        }
        ctx.guardar(nome, valor);
    }

    @E("ao consultar {string} o status deve ser {int}")
    public void aoConsultarOStatusDeveSer(String caminho, int status) {
        executar("GET", caminho, null);
        ctx.resposta().then().statusCode(status);
    }

    private void executar(String metodo, String caminho, Object corpo) {
        String caminhoResolvido = ctx.resolver(caminho);
        Response resposta = ClienteApi.enviar(ctx, metodo, caminhoResolvido, corpo);
        ctx.setUltimaResposta(resposta);

        if ("POST".equalsIgnoreCase(metodo.trim()) && "/empresas".equals(caminhoResolvido)
                && resposta.getStatusCode() == 201) {
            ctx.registrarEmpresaCriada(resposta.jsonPath().getLong("id"));
        }
    }
}
