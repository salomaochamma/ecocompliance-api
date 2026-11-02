package br.com.fiap.ecocompliance.tests.steps;

import br.com.fiap.ecocompliance.tests.support.AguardaApi;
import br.com.fiap.ecocompliance.tests.support.ClienteApi;
import br.com.fiap.ecocompliance.tests.support.Config;
import br.com.fiap.ecocompliance.tests.support.ContextoTeste;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;

public class Hooks {
    private final ContextoTeste ctx;

    public Hooks(ContextoTeste ctx) {
        this.ctx = ctx;
    }

    @BeforeAll
    public static void configurarAmbiente() {
        RestAssured.baseURI = Config.baseUrl();
        RestAssured.config = RestAssured.config()
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        AguardaApi.aguardar(Config.baseUrl(), Config.tempoEsperaApiSegundos());
    }

    @Before
    public void iniciarCenario(Scenario cenario) {
        ctx.setCenario(cenario);
    }

    @After
    public void limparDadosDoCenario() {
        for (Long empresaId : ctx.getEmpresasCriadas()) {
            try {
                ClienteApi.excluirEmpresaComoAdmin(empresaId);
            } catch (RuntimeException ignorada) {
            }
        }
    }
}
