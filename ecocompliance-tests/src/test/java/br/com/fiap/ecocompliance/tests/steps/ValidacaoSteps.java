package br.com.fiap.ecocompliance.tests.steps;

import br.com.fiap.ecocompliance.tests.support.ContextoTeste;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class ValidacaoSteps {
    private static final List<String> DETALHES_INTERNOS = List.of(
            "java.", "jakarta.", "br.com.fiap", "org.springframework", "org.hibernate", "ORA-", "Exception");

    private final ContextoTeste ctx;

    public ValidacaoSteps(ContextoTeste ctx) {
        this.ctx = ctx;
    }

    @Entao("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int esperado) {
        ctx.resposta().then().statusCode(esperado);
    }

    @Entao("o acesso deve ser negado")
    public void oAcessoDeveSerNegado() {
        ctx.resposta().then().statusCode(anyOf(is(401), is(403)));
    }

    @E("a resposta deve seguir o contrato {string}")
    public void aRespostaDeveSeguirOContrato(String contrato) {
        String caminho = "schemas/" + contrato + ".schema.json";
        if (Thread.currentThread().getContextClassLoader().getResource(caminho) == null) {
            throw new IllegalArgumentException("Contrato não encontrado: src/test/resources/" + caminho);
        }
        ctx.resposta().then().body(matchesJsonSchemaInClasspath(caminho));
    }

    @E("o campo {string} deve ser {string}")
    public void oCampoDeveSer(String campo, String esperado) {
        Object atual = ctx.resposta().jsonPath().get(campo);
        String valorEsperado = ctx.resolver(esperado);
        if (atual == null || !valorEsperado.equals(String.valueOf(atual))) {
            throw new AssertionError(String.format("Campo '%s': esperado <%s>, mas veio <%s>",
                    campo, valorEsperado, atual));
        }
    }

    @E("o campo {string} deve ser o número {string}")
    public void oCampoDeveSerONumero(String campo, String esperado) {
        Object atual = ctx.resposta().jsonPath().get(campo);
        if (atual == null) {
            throw new AssertionError("Campo '" + campo + "' ausente ou nulo na resposta");
        }
        BigDecimal valorAtual = new BigDecimal(String.valueOf(atual));
        BigDecimal valorEsperado = new BigDecimal(ctx.resolver(esperado));
        if (valorAtual.compareTo(valorEsperado) != 0) {
            throw new AssertionError(String.format("Campo '%s': esperado o número %s, mas veio %s",
                    campo, valorEsperado.toPlainString(), valorAtual.toPlainString()));
        }
    }

    @E("o campo {string} deve ser nulo")
    public void oCampoDeveSerNulo(String campo) {
        Object atual = ctx.resposta().jsonPath().get(campo);
        if (atual != null) {
            throw new AssertionError("Campo '" + campo + "': esperado null, mas veio <" + atual + ">");
        }
    }

    @E("o campo {string} não deve estar vazio")
    public void oCampoNaoDeveEstarVazio(String campo) {
        Object atual = ctx.resposta().jsonPath().get(campo);
        if (atual == null || String.valueOf(atual).isBlank()) {
            throw new AssertionError("Campo '" + campo + "' veio vazio ou ausente");
        }
    }

    @E("a resposta não deve ter corpo")
    public void aRespostaNaoDeveTerCorpo() {
        String corpo = ctx.resposta().asString();
        if (corpo != null && !corpo.isBlank()) {
            throw new AssertionError("Era esperado corpo vazio (204 No Content), mas veio: " + corpo);
        }
    }

    @E("a resposta não deve expor o campo {string}")
    public void aRespostaNaoDeveExporOCampo(String campo) {
        if (ctx.resposta().asString().contains("\"" + campo + "\"")) {
            throw new AssertionError("A resposta expõe o campo sensível '" + campo + "'");
        }
    }

    @E("a resposta não deve expor detalhes internos da aplicação")
    public void aRespostaNaoDeveExporDetalhesInternos() {
        String corpo = ctx.resposta().asString();
        List<String> expostos = DETALHES_INTERNOS.stream().filter(corpo::contains).toList();
        if (!expostos.isEmpty()) {
            throw new AssertionError("A resposta expõe detalhes internos " + expostos + ": " + corpo);
        }
    }

    @E("a mensagem de erro deve conter {string}")
    public void aMensagemDeErroDeveConter(String trecho) {
        ctx.resposta().then().body("message", containsString(ctx.resolver(trecho)));
    }

    @E("deve haver erro de validação no campo {string}")
    public void deveHaverErroDeValidacaoNoCampo(String campo) {
        List<String> camposComErro = ctx.resposta().jsonPath().getList("fieldErrors.campo");
        if (camposComErro == null || !camposComErro.contains(campo)) {
            throw new AssertionError("Não há erro de validação para o campo '" + campo
                    + "'. Campos com erro: " + camposComErro);
        }
    }

    @E("a mensagem de validação do campo {string} deve ser {string}")
    public void aMensagemDeValidacaoDoCampoDeveSer(String campo, String mensagem) {
        List<Map<String, Object>> erros = ctx.resposta().jsonPath().getList("fieldErrors");
        boolean encontrou = erros != null && erros.stream()
                .anyMatch(erro -> campo.equals(erro.get("campo")) && mensagem.equals(erro.get("mensagem")));
        if (!encontrou) {
            throw new AssertionError("Mensagem de validação esperada para '" + campo + "': <" + mensagem
                    + ">. Erros retornados: " + erros);
        }
    }

    @E("a lista deve conter um item com {string} igual a {string}")
    public void aListaDeveConterUmItem(String campo, String valor) {
        String esperado = ctx.resolver(valor);
        List<String> valores = valoresDaLista(campo);
        if (!valores.contains(esperado)) {
            throw new AssertionError("Nenhum item da lista tem " + campo + " = " + esperado);
        }
    }

    @E("a lista não deve conter item com {string} igual a {string}")
    public void aListaNaoDeveConterItem(String campo, String valor) {
        String esperado = ctx.resolver(valor);
        if (valoresDaLista(campo).contains(esperado)) {
            throw new AssertionError("A lista não deveria conter item com " + campo + " = " + esperado);
        }
    }

    @E("todos os itens da lista devem ter {string} igual a {string}")
    public void todosOsItensDaListaDevemTer(String campo, String valor) {
        String esperado = ctx.resolver(valor);
        List<String> valores = valoresDaLista(campo);
        if (valores.isEmpty()) {
            throw new AssertionError("A lista retornada está vazia");
        }
        List<String> divergentes = valores.stream().filter(v -> !v.equals(esperado)).toList();
        if (!divergentes.isEmpty()) {
            throw new AssertionError("Itens com " + campo + " diferente de " + esperado + ": " + divergentes);
        }
    }

    @E("a lista deve conter {int} itens")
    public void aListaDeveConterItens(int quantidade) {
        List<Object> itens = ctx.resposta().jsonPath().getList("$");
        int total = itens == null ? 0 : itens.size();
        if (total != quantidade) {
            throw new AssertionError("Esperado " + quantidade + " itens na lista, mas vieram " + total);
        }
    }

    @E("o cabeçalho {string} deve terminar com {string}")
    public void oCabecalhoDeveTerminarCom(String cabecalho, String sufixo) {
        String valor = ctx.resposta().getHeader(cabecalho);
        String esperado = ctx.resolver(sufixo);
        if (valor == null || !valor.endsWith(esperado)) {
            throw new AssertionError("Cabeçalho " + cabecalho + ": esperado terminar com <" + esperado
                    + ">, mas veio <" + valor + ">");
        }
    }

    @E("o tempo de resposta deve ser inferior a {int} ms")
    public void oTempoDeRespostaDeveSerInferiorA(int limiteMs) {
        long tempo = ctx.resposta().getTime();
        if (tempo >= limiteMs) {
            throw new AssertionError("Tempo de resposta " + tempo + " ms excedeu o limite de " + limiteMs + " ms");
        }
    }

    private List<String> valoresDaLista(String campo) {
        Response resposta = ctx.resposta();
        List<Object> valores = resposta.jsonPath().getList(campo);
        if (valores == null) {
            throw new AssertionError("A resposta não é uma lista com o campo '" + campo + "': " + resposta.asString());
        }
        return valores.stream().map(String::valueOf).toList();
    }
}
