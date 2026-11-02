# EcoCompliance API — Testes Automatizados (BDD + API + Contrato)

Suíte de testes automatizados da **EcoCompliance API** (Governança e Compliance Ambiental — ESG),
escrita em **BDD com Gherkin em português** e executada com **Cucumber + RestAssured** contra a
API real (Spring Boot + Oracle), com **testes de contrato via JSON Schema**.

| Indicador | Valor |
|---|---|
| Cenários na execução padrão | **87** (positivos e negativos) |
| Cenários de defeitos conhecidos (quarentena, tag `@defeito`) | 4 |
| Endpoints da API cobertos | **31 de 31** |
| Contratos JSON Schema | 13 (`src/test/resources/schemas`) |
| Validações em cada endpoint | status code + corpo JSON + contrato |

---

## Sumário

- [Alinhamento com ESG](#alinhamento-com-esg)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como executar](#como-executar)
- [Filtrando cenários por tag](#filtrando-cenários-por-tag)
- [Relatórios e evidências](#relatórios-e-evidências)
- [Cobertura por endpoint](#cobertura-por-endpoint)
- [Dicionário de passos](#dicionário-de-passos)
- [Marcadores dinâmicos](#marcadores-dinâmicos)
- [Defeitos conhecidos](#defeitos-conhecidos)
- [Solução de problemas](#solução-de-problemas)

---

## Alinhamento com ESG

Os cenários são marcados com tags que mostram a qual pilar ESG cada verificação contribui:

| Tag | Pilar | O que é garantido |
|---|---|---|
| `@ESG-Governanca` | Governança e compliance | Autenticação JWT, controle de acesso por perfil (segregação de funções), integridade (CNPJ e nº de licença únicos), rastreabilidade (todo registro ambiental pertence a uma empresa existente), mensagens de erro que não expõem a implementação |
| `@ESG-Ambiental` | Meio ambiente | Inventário de emissões de CO2, compensações, alerta de licenças vencendo em 30 dias e o relatório ESG consolidado (totais, licenças ativas/vencidas, maior risco) |
| `@ESG-Social` | Inclusão e usabilidade | Mensagens de validação claras e por campo, em português; perfis diferentes de usuário (ADMIN e USER) com acesso adequado |
| `@ESG-Eficiencia` | Eficiência de recursos | Tempo de resposta dos endpoints mais consultados abaixo de 3 s |

Além disso, a própria suíte segue boas práticas de governança: cada cenário cria os seus dados e
**remove tudo ao final** (exclusão em cascata da empresa criada), e o token JWT **nunca** é gravado
nas evidências (aparece como `Bearer ***`).

---

## Tecnologias

| Camada | Ferramenta |
|---|---|
| Linguagem | Java 21 |
| BDD | Cucumber 7.34 — Gherkin em português (`# language: pt`) |
| Execução | JUnit Platform 5.14 (Suite) + Maven Surefire 3.5 |
| Testes de API | RestAssured 5.5.7 |
| Testes de contrato | JSON Schema (draft-04) com `rest-assured/json-schema-validator` |
| Injeção de dependência entre steps | PicoContainer (`cucumber-picocontainer`) |
| Ambiente | Docker Compose: Oracle XE 21 + API + runner de testes |
| CI | GitHub Actions (`.github/workflows/testes-api.yml`) |

---

## Estrutura do projeto

```
ecocompliance-api/                       (raiz do repositório)
├── docker-compose.test.yml              ambiente isolado: Oracle XE + API + testes
├── run-tests.sh / run-tests.ps1         execução em 1 comando (Linux/macOS/WSL e Windows)
├── .github/workflows/testes-api.yml     pipeline de CI
└── ecocompliance-tests/
    ├── Dockerfile                       imagem do runner (Maven + JDK 21)
    ├── pom.xml
    ├── reports/                         relatórios quando executado via Docker
    └── src/test/
        ├── java/br/com/fiap/ecocompliance/tests/
        │   ├── RunCucumberTest.java     Suite JUnit Platform (ponto de entrada)
        │   ├── steps/
        │   │   ├── Hooks.java                 configuração, espera da API e limpeza
        │   │   ├── AutenticacaoSteps.java     login, perfis, tokens inválidos/forjados
        │   │   ├── PreCondicoesSteps.java     "Dado que existe..." (massa de dados)
        │   │   ├── RequisicaoSteps.java       envio das requisições HTTP
        │   │   └── ValidacaoSteps.java        status, corpo JSON, contrato, listas
        │   └── support/
        │       ├── ContextoTeste.java         estado do cenário e marcadores {..}
        │       ├── ClienteApi.java            RestAssured + evidência no relatório
        │       ├── Autenticacao.java          tokens por perfil (cache)
        │       ├── AguardaApi.java            aguarda a API ficar pronta
        │       ├── CorpoJson.java             tabela Gherkin -> corpo JSON
        │       ├── Dados.java                 dados únicos (CNPJ, e-mail, nº licença)
        │       └── Config.java                URL base e tempos de espera
        └── resources/
            ├── junit-platform.properties     configuração do Cucumber
            ├── features/                     cenários BDD (Gherkin)
            │   ├── 01_autenticacao.feature
            │   ├── 02_controle_acesso.feature
            │   ├── 03_empresas.feature
            │   ├── 04_licencas.feature
            │   ├── 05_auditorias.feature
            │   ├── 06_emissoes.feature
            │   ├── 07_compensacoes.feature
            │   ├── 08_relatorio_esg.feature
            │   └── 99_defeitos_conhecidos.feature
            └── schemas/                      contratos JSON Schema
```

---

## Como executar

### Opção 1 — Docker, um comando (recomendado)

Pré-requisito: **Docker** com o plugin **Compose v2** (`docker compose version`). No Windows e no macOS,
o **Docker Desktop** precisa estar instalado e aberto.
Não é preciso ter Java, Maven nem Oracle instalados.

```bash
# na raiz do repositório
./run-tests.sh
```

No Windows (PowerShell, com o Docker Desktop aberto):

```powershell
powershell -ExecutionPolicy Bypass -File .\run-tests.ps1
```

O script:

1. sobe um **Oracle XE 21 novo e vazio** (o Flyway cria as tabelas e os dados iniciais);
2. constrói e sobe a **EcoCompliance API** a partir do código do repositório;
3. constrói o runner e executa a suíte (`mvn test`), aguardando a API ficar pronta;
4. mostra o resultado no terminal, salva os relatórios em `ecocompliance-tests/reports/` e derruba o ambiente.

> A primeira execução baixa as imagens (Oracle, Maven e JDK) e as dependências; pode levar de 5 a 10 minutos.
> As seguintes reaproveitam o cache e ficam bem mais rápidas.

O mesmo, sem o script:

```bash
docker compose -f docker-compose.test.yml up --build --attach api-tests --abort-on-container-exit --exit-code-from api-tests
docker compose -f docker-compose.test.yml down -v
```

### Opção 2 — Maven, com a API já rodando

Pré-requisitos: **JDK 21** e **Maven 3.9+**, e a API rodando (por exemplo, `docker compose up -d --build`
na raiz do repositório, que publica a API em `http://localhost:8080`).

```bash
cd ecocompliance-tests
mvn test                                            # API em http://localhost:8080
mvn test -Dapi.baseUrl=http://outro-host:8080       # outra URL
mvn test -Dcucumber.filter.tags="@smoke"            # só alguns cenários
```

| Configuração | Propriedade (`-D`) | Variável de ambiente | Padrão |
|---|---|---|---|
| URL da API | `api.baseUrl` | `API_BASE_URL` | `http://localhost:8080` |
| Espera máxima pela API (s) | `api.wait.timeoutSeconds` | `API_WAIT_TIMEOUT_SECONDS` | `180` |
| Filtro de tags | `cucumber.filter.tags` | — | `not @defeito` |

Os testes usam os usuários criados automaticamente pela API (`admin@ecocompliance.com` / `admin123`
e `user@ecocompliance.com` / `user123`).

### Opção 3 — IntelliJ IDEA

Abra a pasta `ecocompliance-tests` como projeto Maven e execute a classe `RunCucumberTest`
(botão ▶ ao lado da classe). Com o plugin *Cucumber for Java*, também é possível executar uma
feature ou um cenário isolado clicando no ▶ ao lado dele.

### Opção 4 — GitHub Actions (CI)

O workflow `.github/workflows/testes-api.yml` roda a suíte completa a cada *push* em `main`/`master`
e a cada *pull request*. Também pode ser disparado manualmente em **Actions → Testes de API (BDD) →
Run workflow**, informando um filtro de tags. Ao final:

- o resultado aparece como *check* “Resultado da suíte BDD (Cucumber)” e no resumo do job;
- os relatórios (HTML, JSON e XML) ficam disponíveis no artefato `relatorio-cucumber`.

---

## Filtrando cenários por tag

```bash
./run-tests.sh "@smoke"                  # caminho feliz principal de cada recurso
./run-tests.sh "@empresas"               # uma funcionalidade
./run-tests.sh "@negativo"               # só os cenários de falha
./run-tests.sh "@contrato"               # só os cenários com validação de JSON Schema
./run-tests.sh "@ESG-Ambiental"          # um pilar ESG
./run-tests.sh "@defeito"                # defeitos conhecidos (falham de propósito)
```

No Windows, use o mesmo filtro no script PowerShell: `powershell -ExecutionPolicy Bypass -File .\run-tests.ps1 "@smoke"`.

| Grupo | Tags |
|---|---|
| Funcionalidade | `@autenticacao` `@seguranca` `@empresas` `@licencas` `@auditorias` `@emissoes` `@compensacoes` `@relatorios` |
| Tipo | `@positivo` `@negativo` `@contrato` `@smoke` |
| ESG | `@ESG-Governanca` `@ESG-Ambiental` `@ESG-Social` `@ESG-Eficiencia` |
| Quarentena | `@defeito` `@DEF-01` `@DEF-02` `@DEF-03` `@DEF-04` |

---

## Relatórios e evidências

| Execução | Pasta |
|---|---|
| Docker (`run-tests.sh`) | `ecocompliance-tests/reports/` |
| Maven local | `ecocompliance-tests/target/cucumber-reports/` |
| GitHub Actions | artefato `relatorio-cucumber` |

- **`cucumber.html`** — relatório navegável: cada funcionalidade, cenário e passo, com a
  **requisição e a resposta HTTP anexadas** a cada chamada (evidência de execução).
- **`cucumber.json`** — resultado completo em JSON.
- **`cucumber-junit.xml`** — formato JUnit (usado pelo pipeline de CI).

No terminal, o plugin `summary` mostra ao final o total de cenários e passos executados e aprovados.

---

## Cobertura por endpoint

Cada endpoint tem status code, corpo JSON e contrato validados. Para `DELETE`, o sucesso é
`204` sem corpo; o contrato validado é o de erro (`404`).

| Método | Endpoint | Status testados | Contrato(s) |
|---|---|---|---|
| POST | `/auth/login` | 200, 400, 401 | `auth-response`, `erro` |
| POST | `/auth/register` | 201, 400, 409 | `auth-response`, `erro` |
| GET | `/empresas` | 200, 401/403 | `empresa-lista` |
| GET | `/empresas/{id}` | 200, 404 | `empresa`, `erro` |
| POST | `/empresas` | 201, 400, 403 | `empresa`, `erro` |
| PUT | `/empresas/{id}` | 200, 403, 404 | `empresa` |
| DELETE | `/empresas/{id}` | 204, 403, 404 | `erro` |
| GET | `/licencas` | 200, 401/403 | `licenca-lista` |
| GET | `/licencas/{id}` | 200, 404 | `licenca`, `erro` |
| GET | `/licencas/vencendo` | 200 | `licenca-lista` |
| POST | `/licencas` | 201, 400, 403, 404, 409 | `licenca`, `erro` |
| PUT | `/licencas/{id}` | 200, 403 | `licenca` |
| DELETE | `/licencas/{id}` | 204, 403, 404 | `erro` |
| GET | `/auditorias` | 200, 401/403 | `auditoria-lista` |
| GET | `/auditorias/{id}` | 200, 404 | `auditoria`, `erro` |
| GET | `/auditorias/risco/{nivelRisco}` | 200 | `auditoria-lista` |
| POST | `/auditorias` | 201, 400, 403 | `auditoria`, `erro` |
| PUT | `/auditorias/{id}` | 200, 403 | `auditoria` |
| DELETE | `/auditorias/{id}` | 204, 403, 404 | `erro` |
| GET | `/emissoes` | 200, 401/403 | `emissao-lista` |
| GET | `/emissoes/{id}` | 200, 404 | `emissao`, `erro` |
| GET | `/emissoes/empresa/{empresaId}` | 200, 404 | `emissao-lista`, `erro` |
| POST | `/emissoes` | 201, 400, 403 | `emissao`, `erro` |
| PUT | `/emissoes/{id}` | 200, 403 | `emissao` |
| DELETE | `/emissoes/{id}` | 204, 403, 404 | `erro` |
| GET | `/compensacoes` | 200, 401/403 | `compensacao-lista` |
| GET | `/compensacoes/{id}` | 200, 404 | `compensacao`, `erro` |
| POST | `/compensacoes` | 201, 400, 403, 404 | `compensacao`, `erro` |
| PUT | `/compensacoes/{id}` | 200, 403 | `compensacao` |
| DELETE | `/compensacoes/{id}` | 204, 403, 404 | `erro` |
| GET | `/relatorios/empresa/{empresaId}` | 200, 401/403, 404 | `relatorio`, `erro` |

Os contratos usam `additionalProperties: false`: se a API passar a devolver um campo não previsto
(por exemplo, a senha de um usuário), o teste de contrato falha.

---

## Dicionário de passos

Passos disponíveis para escrever novos cenários (os textos entre aspas aceitam marcadores).

**Contexto e pré-condições**

| Passo |
|---|
| `Dado que estou autenticado como "ADMIN"` (ou `"USER"`) |
| `Dado que não estou autenticado` |
| `Dado que estou usando um token inválido` |
| `Dado que estou usando um token forjado para o e-mail "..."` |
| `Dado que existe um usuário cadastrado com o e-mail "...", senha "..." e perfil "USER"` |
| `Dado que existe uma empresa cadastrada` → guarda `{empresa.id}`, `{empresa.nome}`, `{empresa.cnpj}` |
| `Dado que existe uma licença "ATIVA" com validade daqui a 15 dias para essa empresa` → `{licenca.id}`, `{licenca.numero}` |
| `Dado que existe uma licença "ATIVA" com validade daqui a 15 dias para essa empresa, identificada como "x"` → `{x.id}`, `{x.numero}` |
| `Dado que existe uma licença "VENCIDA" com validade expirada há 10 dias para essa empresa` |
| `Dado que existe uma auditoria com risco "ALTO" para essa empresa` → `{auditoria.id}` |
| `Dado que existe uma emissão de "100.5" tCO2e para essa empresa` → `{emissao.id}` |
| `Dado que existe uma compensação de "40" tCO2e para essa empresa` → `{compensacao.id}` |

**Ações**

| Passo |
|---|
| `Quando faço login com o e-mail "..." e a senha "..."` |
| `Quando envio uma requisição GET para "/rota"` (GET, POST, PUT ou DELETE) |
| `Quando envio uma requisição POST para "/rota" com o corpo:` + tabela `campo \| valor` |
| `Quando envio uma requisição POST para "/rota" com o corpo bruto:` + texto entre `"""` |
| `E guardo o campo "id" da resposta como "nome"` → `{nome}` |
| `E ao consultar "/rota" o status deve ser 404` |

**Verificações**

| Passo |
|---|
| `Então o status da resposta deve ser 201` |
| `Então o acesso deve ser negado` (401 ou 403) |
| `E a resposta deve seguir o contrato "empresa"` (arquivo `schemas/empresa.schema.json`) |
| `E o campo "nome" deve ser "..."` (aceita caminhos como `"empresa.id"`) |
| `E o campo "quantidadeCo2" deve ser o número "750.5"` |
| `E o campo "maiorNivelRisco" deve ser nulo` |
| `E o campo "token" não deve estar vazio` |
| `E a resposta não deve ter corpo` |
| `E a resposta não deve expor o campo "senha"` |
| `E a resposta não deve expor detalhes internos da aplicação` |
| `E a mensagem de erro deve conter "..."` |
| `E deve haver erro de validação no campo "cnpj"` |
| `E a mensagem de validação do campo "cnpj" deve ser "..."` |
| `E a lista deve conter um item com "id" igual a "..."` |
| `E a lista não deve conter item com "id" igual a "..."` |
| `E todos os itens da lista devem ter "status" igual a "ATIVA"` |
| `E a lista deve conter 2 itens` |
| `E o cabeçalho "Location" deve terminar com "..."` |
| `E o tempo de resposta deve ser inferior a 3000 ms` |

---

## Marcadores dinâmicos

| Marcador | Valor |
|---|---|
| `{hoje}`, `{hoje+15}`, `{hoje-30}` | data relativa a hoje (`yyyy-MM-dd`) |
| `{cnpjUnico}` | CNPJ aleatório no formato `00.000.000/0000-00` |
| `{emailUnico}` | e-mail aleatório |
| `{numeroLicencaUnico}` | número de licença aleatório |
| `{empresa.id}`, `{licenca.numero}`... | valores guardados pelos passos anteriores do cenário |
| `[vazio]` / `[nulo]` | envia `""` / `null` no corpo JSON |

Os valores únicos são gerados no primeiro uso e se mantêm iguais até o fim do cenário, então a suíte
pode ser executada várias vezes sem conflito de dados.

---

## Defeitos conhecidos

A automação encontrou divergências entre o comportamento esperado e o atual da API. Elas estão
descritas em `99_defeitos_conhecidos.feature` com a tag `@defeito`: **ficam fora da execução padrão**
e servem como testes de regressão — passam a ficar verdes quando a correção for feita.

| ID | Comportamento esperado | Comportamento atual | Causa / correção sugerida |
|---|---|---|---|
| DEF-01 | Requisição sem token → `401 Unauthorized` (como diz o README da API) | `403 Forbidden` | O `SecurityConfig` não define `authenticationEntryPoint`, então o Spring Security usa o padrão (403). Correção: `.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))` |
| DEF-02 | Nível de risco inválido (`/auditorias/risco/CRITICO`) → `400` sem detalhes internos | `500` com o nome de classes Java na mensagem | O `@ExceptionHandler(Exception.class)` captura o `MethodArgumentTypeMismatchException`. Correção: handler específico retornando 400 e mensagem genérica |
| DEF-03 | JSON malformado → `400 Bad Request` | `500 Internal Server Error` | Mesmo motivo, com `HttpMessageNotReadableException` |
| DEF-04 | Mensagens de erro escritas corretamente (ex.: "Licença não encontrada") | Caracteres faltando: "Licena", "Emisso", "Compensao", "anterior  data" | Corrigir os textos em `LicencaService`, `EmissaoService` e `CompensacaoService` (salvar os arquivos em UTF-8) |

Para executá-los: `./run-tests.sh "@defeito"` ou `mvn test -Dcucumber.filter.tags="@defeito"`.

---

## Solução de problemas

| Situação | O que fazer |
|---|---|
| `A API não ficou disponível ... após 300s` | Veja o log que o script mostra ao final. Na primeira execução o Oracle pode demorar: aumente `API_WAIT_TIMEOUT_SECONDS` no `docker-compose.test.yml` |
| Mac com processador Apple (ARM) | A imagem `gvenzl/oracle-xe:21-slim` é só x86. Troque por `gvenzl/oracle-free:23-slim` (tem versão ARM) e use `DB_URL: jdbc:oracle:thin:@oracle-db:1521/FREEPDB1` |
| Windows bloqueia o `.ps1` ("execução de scripts foi desabilitada") | Rode com `powershell -ExecutionPolicy Bypass -File .\run-tests.ps1`, como nas instruções acima |
| `o Docker não respondeu` | Abra o Docker Desktop e espere aparecer *Engine running* antes de rodar o script |
| Não consigo apagar `reports/` no Linux | Os arquivos são criados pelo container como root: `sudo rm -rf ecocompliance-tests/reports` |
| `mvn test` local falha em autenticação | Confirme que a API criou os usuários iniciais (`DataInitializer`) e que está em `http://localhost:8080` |
