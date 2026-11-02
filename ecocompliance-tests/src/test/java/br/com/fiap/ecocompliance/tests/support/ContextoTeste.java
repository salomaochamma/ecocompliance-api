package br.com.fiap.ecocompliance.tests.support;

import io.cucumber.java.Scenario;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextoTeste {
    private static final Pattern MARCADOR = Pattern.compile("\\{([^{}]+)}");
    private static final Pattern DATA_RELATIVA = Pattern.compile("hoje([+-]\\d+)?");

    private final Map<String, String> variaveis = new HashMap<>();
    private final List<Long> empresasCriadas = new ArrayList<>();
    private Response ultimaResposta;
    private String token;
    private Scenario cenario;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Response resposta() {
        if (ultimaResposta == null) {
            throw new IllegalStateException("Nenhuma requisição foi enviada neste cenário.");
        }
        return ultimaResposta;
    }

    public void setUltimaResposta(Response resposta) {
        this.ultimaResposta = resposta;
    }

    public void guardar(String nome, Object valor) {
        variaveis.put(nome, String.valueOf(valor));
    }

    public String valor(String nome) {
        String valor = variaveis.get(nome);
        if (valor == null) {
            throw new IllegalStateException("Variável '" + nome + "' não foi definida neste cenário. "
                    + "Disponíveis: " + variaveis.keySet());
        }
        return valor;
    }

    public String resolver(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher marcador = MARCADOR.matcher(texto);
        StringBuilder resultado = new StringBuilder();
        while (marcador.find()) {
            String valor = valorDoMarcador(marcador.group(1).trim());
            marcador.appendReplacement(resultado, Matcher.quoteReplacement(valor));
        }
        marcador.appendTail(resultado);
        return resultado.toString();
    }

    private String valorDoMarcador(String nome) {
        Matcher data = DATA_RELATIVA.matcher(nome);
        if (data.matches()) {
            int dias = data.group(1) == null ? 0 : Integer.parseInt(data.group(1));
            return LocalDate.now().plusDays(dias).toString();
        }
        if (variaveis.containsKey(nome)) {
            return variaveis.get(nome);
        }
        String gerado = switch (nome) {
            case "cnpjUnico" -> Dados.cnpjUnico();
            case "emailUnico" -> Dados.emailUnico();
            case "numeroLicencaUnico" -> Dados.numeroLicencaUnico();
            default -> throw new IllegalStateException("Marcador {" + nome + "} não foi definido neste cenário. "
                    + "Disponíveis: " + variaveis.keySet());
        };
        variaveis.put(nome, gerado);
        return gerado;
    }

    public void registrarEmpresaCriada(Long id) {
        empresasCriadas.add(id);
    }

    public List<Long> getEmpresasCriadas() {
        return List.copyOf(empresasCriadas);
    }

    public void setCenario(Scenario cenario) {
        this.cenario = cenario;
    }

    public void anexar(String titulo, String conteudo) {
        if (cenario != null) {
            cenario.attach(conteudo, "text/plain", titulo);
        }
    }
}
