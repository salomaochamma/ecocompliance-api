package br.com.fiap.ecocompliance.tests.steps;

import br.com.fiap.ecocompliance.tests.support.ClienteApi;
import br.com.fiap.ecocompliance.tests.support.ContextoTeste;
import br.com.fiap.ecocompliance.tests.support.Dados;
import io.cucumber.java.pt.Dado;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreCondicoesSteps {
    private final ContextoTeste ctx;

    public PreCondicoesSteps(ContextoTeste ctx) {
        this.ctx = ctx;
    }

    @Dado("que existe uma empresa cadastrada")
    public void queExisteUmaEmpresaCadastrada() {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", Dados.nomeEmpresaUnico());
        corpo.put("cnpj", Dados.cnpjUnico());
        corpo.put("setor", "Energia Renovável");
        corpo.put("emailResponsavel", "esg." + Dados.sufixo(8) + "@empresa-qa.com.br");

        Response resposta = criar("/empresas", corpo);
        long id = resposta.jsonPath().getLong("id");
        ctx.registrarEmpresaCriada(id);
        ctx.guardar("empresa.id", id);
        ctx.guardar("empresa.nome", resposta.jsonPath().getString("nome"));
        ctx.guardar("empresa.cnpj", resposta.jsonPath().getString("cnpj"));
    }

    @Dado("que existe uma licença {string} com validade daqui a {int} dias para essa empresa")
    public void queExisteUmaLicenca(String status, int dias) {
        criarLicenca(status, dias, "licenca");
    }

    @Dado("que existe uma licença {string} com validade daqui a {int} dias para essa empresa, identificada como {string}")
    public void queExisteUmaLicencaIdentificada(String status, int dias, String apelido) {
        criarLicenca(status, dias, apelido);
    }

    @Dado("que existe uma licença {string} com validade expirada há {int} dias para essa empresa")
    public void queExisteUmaLicencaExpirada(String status, int dias) {
        criarLicenca(status, -dias, "licenca");
    }

    @Dado("que existe uma auditoria com risco {string} para essa empresa")
    public void queExisteUmaAuditoria(String nivelRisco) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("empresaId", empresaId());
        corpo.put("dataAuditoria", LocalDate.now().minusDays(15).toString());
        corpo.put("resultado", "Auditoria interna de conformidade");
        corpo.put("observacoes", "Registro criado pela automação de testes (QA)");
        corpo.put("nivelRisco", nivelRisco);

        Response resposta = criar("/auditorias", corpo);
        ctx.guardar("auditoria.id", resposta.jsonPath().getLong("id"));
    }

    @Dado("que existe uma emissão de {string} tCO2e para essa empresa")
    public void queExisteUmaEmissao(String quantidade) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("empresaId", empresaId());
        corpo.put("quantidadeCo2", new BigDecimal(quantidade));
        corpo.put("fonteEmissao", "Frota de veículos (Escopo 1)");
        corpo.put("dataRegistro", LocalDate.now().minusDays(20).toString());
        corpo.put("unidadeMedida", "tCO2e");

        Response resposta = criar("/emissoes", corpo);
        ctx.guardar("emissao.id", resposta.jsonPath().getLong("id"));
    }

    @Dado("que existe uma compensação de {string} tCO2e para essa empresa")
    public void queExisteUmaCompensacao(String quantidade) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("empresaId", empresaId());
        corpo.put("tipoCompensacao", "Reflorestamento");
        corpo.put("descricao", "Plantio de mudas nativas da Mata Atlântica (QA)");
        corpo.put("quantidadeCompensada", new BigDecimal(quantidade));
        corpo.put("dataAcao", LocalDate.now().minusDays(10).toString());

        Response resposta = criar("/compensacoes", corpo);
        ctx.guardar("compensacao.id", resposta.jsonPath().getLong("id"));
    }

    private void criarLicenca(String status, int diasAteValidade, String apelido) {
        LocalDate hoje = LocalDate.now();
        LocalDate validade = hoje.plusDays(diasAteValidade);
        LocalDate emissao = (validade.isAfter(hoje) ? hoje : validade).minusDays(365);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("empresaId", empresaId());
        corpo.put("tipoLicenca", "Licença de Operação");
        corpo.put("numeroLicenca", Dados.numeroLicencaUnico());
        corpo.put("dataEmissao", emissao.toString());
        corpo.put("dataValidade", validade.toString());
        corpo.put("status", status);
        corpo.put("orgaoEmissor", "CETESB");

        Response resposta = criar("/licencas", corpo);
        ctx.guardar(apelido + ".id", resposta.jsonPath().getLong("id"));
        ctx.guardar(apelido + ".numero", resposta.jsonPath().getString("numeroLicenca"));
    }

    private long empresaId() {
        return Long.parseLong(ctx.valor("empresa.id"));
    }

    private Response criar(String caminho, Map<String, Object> corpo) {
        Response resposta = ClienteApi.enviarComoAdmin(ctx, "POST", caminho, corpo);
        if (resposta.getStatusCode() != 201) {
            throw new IllegalStateException("Pré-condição falhou: POST " + caminho + " retornou HTTP "
                    + resposta.getStatusCode() + " - " + resposta.asString());
        }
        return resposta;
    }
}
