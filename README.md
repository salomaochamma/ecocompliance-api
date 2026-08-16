# EcoCompliance API

API RESTful para **Governança e Compliance Ambiental (ESG)**, desenvolvida em **Java 21 + Spring Boot 3** com **Oracle Database**, **Flyway**, **Spring Security + JWT** e **Docker**.

> Atividade acadêmica – FIAP – Java Advanced.

---

## Sumário

- [Tema ESG](#tema-esg)
- [Objetivo do sistema](#objetivo-do-sistema)
- [Tecnologias](#tecnologias)
- [Arquitetura em camadas](#arquitetura-em-camadas)
- [Como rodar](#como-rodar)
  - [Pré-requisitos](#pré-requisitos)
  - [Configurar Oracle (FIAP ou local)](#configurar-oracle-fiap-ou-local)
  - [Executando localmente (Maven)](#executando-localmente-maven)
  - [Executando com Docker](#executando-com-docker)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Usuários iniciais](#usuários-iniciais)
- [Endpoints disponíveis](#endpoints-disponíveis)
- [Paginação nos endpoints de listagem](#paginação-nos-endpoints-de-listagem)
- [Exemplos de JSON para testar](#exemplos-de-json-para-testar)
- [Testes unitários](#testes-unitários)
- [Estrutura de pacotes](#estrutura-de-pacotes)
- [Boas práticas aplicadas](#boas-práticas-aplicadas)
- [Autores](#autores)

---

## Tema ESG

**Governança e Compliance Ambiental.**

A **EcoCompliance API** apoia empresas na gestão de sua conformidade ambiental, oferecendo um cadastro centralizado para:

- Empresas e seus setores de atuação
- Licenças ambientais e seus prazos de validade
- Auditorias internas com classificação de risco
- Inventário de emissões de carbono (Escopo 1, 2 e 3)
- Ações de compensação ambiental (reflorestamento, créditos de carbono, energia renovável)
- **Agendamentos de redução de carbono** (iniciativas programadas com meta de CO₂ e prazo)

A API gera ainda um **relatório consolidado por empresa** com KPIs ESG e um **painel de indicadores globais de sustentabilidade** com métricas agregadas de todas as empresas cadastradas.

---

## Objetivo do sistema

Permitir que uma organização:

1. Mantenha um inventário confiável de **licenças ambientais** com alerta de vencimento.
2. Registre **auditorias** classificadas por nível de risco (BAIXO, MEDIO, ALTO).
3. Acompanhe suas **emissões de carbono** por fonte e período.
4. Documente as **compensações ambientais** realizadas.
5. **Agende e acompanhe iniciativas de redução de carbono** com meta, prazo e status.
6. Gere um **relatório ESG** consolidado por empresa.
7. Consulte **indicadores globais de sustentabilidade** agregando dados de todas as empresas.

---

## Tecnologias

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | **Java 21** (testado com JDK 21 e JDK 24) |
| Framework | **Spring Boot 3.3.x** |
| Build | **Maven** |
| Web | Spring Web (REST) |
| Persistência | Spring Data JPA + Hibernate |
| Validação | Spring Validation (Jakarta Bean Validation) |
| Segurança | Spring Security + **JWT (jjwt 0.12)** |
| Banco | **Oracle Database** (driver `ojdbc11`) |
| Migrações | **Flyway** (`flyway-core` + `flyway-database-oracle`) |
| Documentação | **SpringDoc OpenAPI 2.6** (Swagger UI) |
| Boilerplate | **Lombok 1.18.38** |
| Container | **Docker** + **docker-compose** |
| Testes | JUnit 5, Mockito, Spring Boot Test, Spring Security Test |

---

## Arquitetura em camadas

```
controller   → recebe requisições HTTP, valida, delega
service      → regras de negócio, transações
repository   → acesso a dados (Spring Data JPA)
entity       → modelo de domínio (JPA)
dto          → objetos de entrada/saída (Records)
exception    → exceções customizadas + GlobalExceptionHandler
config       → SecurityConfig, OpenApiConfig, DataInitializer
security     → JwtService, JwtAuthenticationFilter
```

> **Princípio aplicado:** os controllers nunca expõem entidades JPA. Toda E/S passa por DTOs (`Record`), validados com Bean Validation.

---

## Como rodar

### Pré-requisitos

- **JDK 21+** (testado com JDK 21.0.11 Microsoft OpenJDK e JDK 24.0.2 Oracle)
- **Maven 3.9+**
- **Acesso a um Oracle Database** (FIAP, Oracle XE local, ou via docker-compose)
- (Opcional) **Docker Desktop** para rodar com containers

### Configurar Oracle (FIAP ou local)

A API usa as variáveis de ambiente abaixo (com defaults para FIAP em `application.properties`):

| Variável | Default | Descrição |
|----------|---------|-----------|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` | JDBC URL do Oracle |
| `DB_USERNAME` | `rm564765` | Usuário Oracle |
| `DB_PASSWORD` | `senha` | Senha do banco |
| `JWT_SECRET` | chave Base64 inclusa | Chave HMAC-SHA para assinar os tokens |

Edite `src/main/resources/application.properties` com user/senha **ou** exporte as variáveis antes de rodar:

```bash
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USERNAME="rm564765"
export DB_PASSWORD="senha"
```

> O **Flyway** executa automaticamente as 3 migrações na primeira inicialização:
> - `V1` — cria todas as tabelas e índices
> - `V2` — popula dados de teste (empresas, licenças, auditorias, emissões, compensações)
> - `V3` — cria a tabela de agendamentos de redução de carbono e popula exemplos
>
> Os usuários `admin` e `user` são criados pela classe `DataInitializer` com senha em BCrypt gerado pelo Spring Security.

### Executando localmente (Maven)

```bash
# 1. Compilar
mvn clean package -DskipTests

# 2. Executar
mvn spring-boot:run
```

A API sobe em **http://localhost:8080**.

### Executando com Docker

O `docker-compose.yml` sobe **dois containers**: Oracle XE 21 + a API.

```bash
# Build + up
docker compose up --build

# Em background
docker compose up -d --build

# Logs
docker compose logs -f ecocompliance-api

# Derrubar tudo (apaga o volume do Oracle)
docker compose down -v
```

> O primeiro start do Oracle XE leva **2-3 minutos**. A API aguarda o `healthcheck` antes de subir.

Build apenas da imagem da API:

```bash
docker build -t ecocompliance-api:latest .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1" \
  -e DB_USERNAME=ecocompliance \
  -e DB_PASSWORD=ecocompliance123 \
  ecocompliance-api:latest
```

---

## Documentação interativa (Swagger)

Com a aplicação rodando, acesse:

| URL | Descrição |
|-----|-----------|
| `http://localhost:8080/swagger-ui/index.html` | Interface visual Swagger UI |
| `http://localhost:8080/v3/api-docs` | Especificação OpenAPI em JSON |

> O Swagger está liberado sem autenticação. Para testar endpoints protegidos, clique em **Authorize** e informe o token JWT no formato `Bearer <token>`.

---

## Usuários iniciais

Criados automaticamente pela `DataInitializer` no primeiro startup:

| Email | Senha | Role | Permissões |
|-------|-------|------|------------|
| `admin@ecocompliance.com` | `admin123` | `ADMIN` | Tudo (POST, PUT, DELETE, GET) |
| `user@ecocompliance.com`  | `user123`  | `USER`  | Apenas leitura (GET) |

### Como obter o token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ecocompliance.com","senha":"admin123"}'
```

Resposta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "email": "admin@ecocompliance.com",
  "nome": "Administrador EcoCompliance",
  "role": "ADMIN"
}
```

Use o token nas demais chamadas:

```
Authorization: Bearer <token>
```

---

## Endpoints disponíveis

### Autenticação (públicos)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/auth/register` | Registra novo usuário |
| `POST` | `/auth/login` | Autentica e retorna JWT |

### Empresas

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/empresas` | USER, ADMIN | Lista paginada |
| `GET` | `/empresas/{id}` | USER, ADMIN | Busca por ID |
| `POST` | `/empresas` | ADMIN | Cria empresa |
| `PUT` | `/empresas/{id}` | ADMIN | Atualiza empresa |
| `DELETE` | `/empresas/{id}` | ADMIN | Remove empresa |

### Licenças ambientais

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/licencas` | USER, ADMIN | Lista paginada |
| `GET` | `/licencas/{id}` | USER, ADMIN | Busca por ID |
| `GET` | `/licencas/vencendo` | USER, ADMIN | Licenças que vencem em até 30 dias |
| `POST` | `/licencas` | ADMIN | Cria licença |
| `PUT` | `/licencas/{id}` | ADMIN | Atualiza licença |
| `DELETE` | `/licencas/{id}` | ADMIN | Remove licença |

### Auditorias

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/auditorias` | USER, ADMIN | Lista paginada |
| `GET` | `/auditorias/{id}` | USER, ADMIN | Busca por ID |
| `GET` | `/auditorias/risco/{nivelRisco}` | USER, ADMIN | Filtra por nível de risco |
| `POST` | `/auditorias` | ADMIN | Cria auditoria |
| `PUT` | `/auditorias/{id}` | ADMIN | Atualiza auditoria |
| `DELETE` | `/auditorias/{id}` | ADMIN | Remove auditoria |

### Emissões de carbono

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/emissoes` | USER, ADMIN | Lista paginada |
| `GET` | `/emissoes/{id}` | USER, ADMIN | Busca por ID |
| `GET` | `/emissoes/empresa/{empresaId}` | USER, ADMIN | Filtra por empresa |
| `POST` | `/emissoes` | ADMIN | Registra emissão |
| `PUT` | `/emissoes/{id}` | ADMIN | Atualiza emissão |
| `DELETE` | `/emissoes/{id}` | ADMIN | Remove emissão |

### Compensações ambientais

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/compensacoes` | USER, ADMIN | Lista paginada |
| `GET` | `/compensacoes/{id}` | USER, ADMIN | Busca por ID |
| `POST` | `/compensacoes` | ADMIN | Registra compensação |
| `PUT` | `/compensacoes/{id}` | ADMIN | Atualiza compensação |
| `DELETE` | `/compensacoes/{id}` | ADMIN | Remove compensação |

### Agendamentos de redução de carbono

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/agendamentos-reducao` | USER, ADMIN | Lista paginada |
| `GET` | `/agendamentos-reducao/{id}` | USER, ADMIN | Busca por ID |
| `GET` | `/agendamentos-reducao/empresa/{empresaId}` | USER, ADMIN | Filtra por empresa |
| `GET` | `/agendamentos-reducao/status/{status}` | USER, ADMIN | Filtra por status |
| `POST` | `/agendamentos-reducao` | ADMIN | Cria agendamento |
| `PUT` | `/agendamentos-reducao/{id}` | ADMIN | Atualiza agendamento |
| `DELETE` | `/agendamentos-reducao/{id}` | ADMIN | Remove agendamento |

> **Regras de negócio:** a `dataFim` deve ser posterior à `dataInicio`; a `dataInicio` não pode ser no passado. Agendamentos com status `CONCLUIDO` ou `CANCELADO` não podem ser editados.
>
> **Status aceitos:** `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`.

### Relatórios ESG

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/relatorios/empresa/{empresaId}` | USER, ADMIN | Relatório consolidado por empresa |

### Indicadores de sustentabilidade

| Método | Rota | Roles | Descrição |
|--------|------|-------|-----------|
| `GET` | `/indicadores-sustentabilidade` | USER, ADMIN | Métricas ESG globais de todas as empresas |

### Códigos HTTP retornados

| Status | Significado |
|--------|-------------|
| `200 OK` | Consulta/atualização bem-sucedida |
| `201 Created` | Recurso criado (POST) |
| `204 No Content` | Exclusão bem-sucedida (DELETE) |
| `400 Bad Request` | Erro de validação ou regra de negócio |
| `401 Unauthorized` | Token ausente, inválido ou expirado |
| `403 Forbidden` | Usuário autenticado mas sem permissão |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Recurso duplicado (CNPJ, e-mail, número de licença) |

---

## Paginação nos endpoints de listagem

Os seis endpoints de listagem (`GET /empresas`, `/emissoes`, `/auditorias`, `/compensacoes`, `/licencas`, `/agendamentos-reducao`) suportam paginação e ordenação via query params:

| Parâmetro | Tipo | Default | Descrição |
|-----------|------|---------|-----------|
| `page` | int | `0` | Número da página (base 0) |
| `size` | int | `20` | Itens por página |
| `sort` | string | `id,asc` | Campo e direção de ordenação |

**Exemplos:**

```
GET /empresas?page=0&size=10&sort=nome,asc
GET /emissoes?page=1&size=5&sort=dataRegistro,desc
GET /licencas?page=0&size=20&sort=dataValidade,asc
```

**Formato da resposta paginada:**

```json
{
  "content": [ ... ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "empty": false }
  },
  "totalElements": 42,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "empty": false
}
```

> Os dados ficam no campo `content`. Os metadados de paginação (`totalElements`, `totalPages`, etc.) permitem que o cliente construa navegação.

---

## Exemplos de JSON para testar

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@ecocompliance.com",
  "senha": "admin123"
}
```

### Criar empresa

```http
POST /empresas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nome": "Nova Empresa Verde Ltda",
  "cnpj": "99.888.777/0001-66",
  "setor": "Tecnologia",
  "emailResponsavel": "esg@novaempresa.com.br"
}
```

### Criar licença ambiental

```http
POST /licencas
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "tipoLicenca": "Licença de Operação",
  "numeroLicenca": "LO-2025-501",
  "dataEmissao": "2025-04-01",
  "dataValidade": "2027-04-01",
  "status": "ATIVA",
  "orgaoEmissor": "CETESB"
}
```

> `status` aceita: `ATIVA`, `VENCIDA`, `SUSPENSA`, `CANCELADA`.

### Criar emissão de carbono

```http
POST /emissoes
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "quantidadeCo2": 750.5,
  "fonteEmissao": "Combustão industrial",
  "dataRegistro": "2025-04-30",
  "unidadeMedida": "tCO2e"
}
```

### Criar agendamento de redução de carbono

```http
POST /agendamentos-reducao
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "titulo": "Instalação de Painéis Solares",
  "descricao": "Instalação de 300 kWp de energia fotovoltaica na planta industrial",
  "tipoIniciativa": "Energia Renovável",
  "metaCo2": 450.00,
  "dataInicio": "2026-07-01",
  "dataFim": "2026-12-31",
  "status": "PENDENTE"
}
```

> `dataInicio` não pode ser no passado (`@FutureOrPresent`). `dataFim` deve ser futura (`@Future`) e posterior à `dataInicio`.

### Atualizar status de agendamento

```http
PUT /agendamentos-reducao/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "titulo": "Instalação de Painéis Solares",
  "descricao": "Instalação de 300 kWp de energia fotovoltaica na planta industrial",
  "tipoIniciativa": "Energia Renovável",
  "metaCo2": 450.00,
  "dataInicio": "2026-07-01",
  "dataFim": "2026-12-31",
  "status": "EM_ANDAMENTO"
}
```

### Listar emissões com paginação

```http
GET /emissoes?page=0&size=5&sort=dataRegistro,desc
Authorization: Bearer <token>
```

### Indicadores globais de sustentabilidade

```http
GET /indicadores-sustentabilidade
Authorization: Bearer <token>
```

Resposta:

```json
{
  "totalEmpresas": 3,
  "totalEmissoesCo2": 7051.5000,
  "totalCarbonoCompensado": 4200.0000,
  "saldoLiquidoCarbono": -2851.5000,
  "licencasAtivas": 4,
  "licencasVencidas": 1,
  "totalAuditorias": 3,
  "auditoriasAltoRisco": 1,
  "agendamentosEmAndamento": 1,
  "agendamentosPendentes": 2
}
```

> `saldoLiquidoCarbono` = compensado − emitido. Valor negativo indica que as emissões ainda superam as compensações.

### Relatório ESG por empresa

```http
GET /relatorios/empresa/1
Authorization: Bearer <token>
```

Resposta:

```json
{
  "empresa": {
    "id": 1,
    "nome": "Verde Indústria S.A.",
    "cnpj": "12.345.678/0001-90",
    "setor": "Manufatura",
    "emailResponsavel": "sustentabilidade@verdeindustria.com.br",
    "createdAt": "2025-04-30T10:15:30"
  },
  "totalEmissoesCarbono": 2231.2500,
  "totalCarbonoCompensado": 1500.0000,
  "quantidadeLicencasAtivas": 2,
  "quantidadeLicencasVencidas": 0,
  "quantidadeAuditorias": 1,
  "maiorNivelRisco": "MEDIO"
}
```

### Resposta de erro padronizada

Validação:

```json
{
  "timestamp": "2025-04-30T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação nos campos",
  "path": "/agendamentos-reducao",
  "fieldErrors": [
    { "campo": "dataInicio", "mensagem": "A data de início não pode ser no passado" },
    { "campo": "dataFim", "mensagem": "A data de fim deve ser futura" }
  ]
}
```

Regra de negócio:

```json
{
  "timestamp": "2025-04-30T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Não é possível editar um agendamento com status CONCLUIDO",
  "path": "/agendamentos-reducao/1",
  "fieldErrors": null
}
```

---

## Testes

O projeto conta com **111 testes** (0 falhas) divididos em duas camadas.

### Testes unitários de service — Mockito puro (`@ExtendWith(MockitoExtension.class)`)

| Classe | Testes | O que cobre |
|--------|--------|-------------|
| `EmpresaServiceTest` | 9 | CRUD completo, validação de CNPJ duplicado |
| `EmissaoServiceTest` | 8 | CRUD, filtro por empresa, empresa inexistente |
| `AgendamentoServiceTest` | 14 | CRUD, filtro por empresa/status, bloqueio de edição por status, validação de datas |
| `AuditoriaServiceTest` | 10 | CRUD, filtro por nível de risco, empresa inexistente |
| `CompensacaoServiceTest` | 8 | CRUD, empresa inexistente |
| `LicencaServiceTest` | 9 | CRUD, licenças vencendo, validação de datas (validade < emissão) |
| `IndicadoresServiceTest` | 3 | Agregação de todos os indicadores, saldo positivo/zero |
| `RelatorioServiceTest` | 5 | Relatório completo, sem auditorias, cálculo de maior risco (BAIXO/MEDIO/ALTO) |

### Testes de controller — `@WebMvcTest` + MockMvc

| Classe | Testes | O que cobre |
|--------|--------|-------------|
| `EmpresaControllerTest` | 6 | GET lista paginada, GET por ID, 404, POST (201), 403 |
| `EmissaoControllerTest` | 6 | GET lista paginada, GET por empresa, 404, POST (201), 403 |
| `AgendamentoControllerTest` | 8 | GET lista paginada, GET por status, 404, POST (201), 403, PUT com BusinessException (400), DELETE (204) |
| `AuditoriaControllerTest` | 8 | GET lista paginada, GET por risco, 404, POST (201), 403, DELETE (204) |
| `CompensacaoControllerTest` | 8 | GET lista paginada, GET por ID, 404, POST (201), PUT (200), 403, DELETE (204) |
| `LicencaControllerTest` | 9 | GET lista paginada, GET vencendo, 404, POST (201), POST datas inválidas (400), 403, DELETE (204) |

Para executar:

```bash
# Todos os testes
mvn test

# Apenas os testes de service
mvn test -Dtest="*ServiceTest"

# Apenas os testes de controller
mvn test -Dtest="*ControllerTest"

# Uma classe específica
mvn test -Dtest="LicencaServiceTest"
```

> - Testes de service usam Mockito com `mock-maker-subclass`, compatível com JDK 21+.
> - Testes de controller usam `@WebMvcTest` + `@Import(SecurityConfig.class)` para carregar as regras de autorização, e `@WithMockUser` para simular autenticação.
> - Respostas de listagem retornam `Page<T>` — os asserts de controller usam `$.content[0].campo`.

---

## Estrutura de pacotes

```
ecocompliance/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/br/com/fiap/ecocompliance/
    │   │   ├── EcoComplianceApplication.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── EmpresaController.java
    │   │   │   ├── LicencaController.java
    │   │   │   ├── AuditoriaController.java
    │   │   │   ├── EmissaoController.java
    │   │   │   ├── CompensacaoController.java
    │   │   │   ├── AgendamentoController.java
    │   │   │   ├── RelatorioController.java
    │   │   │   └── IndicadoresController.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── EmpresaService.java
    │   │   │   ├── LicencaService.java
    │   │   │   ├── AuditoriaService.java
    │   │   │   ├── EmissaoService.java
    │   │   │   ├── CompensacaoService.java
    │   │   │   ├── AgendamentoService.java
    │   │   │   ├── RelatorioService.java
    │   │   │   └── IndicadoresService.java
    │   │   ├── repository/
    │   │   │   ├── EmpresaRepository.java
    │   │   │   ├── LicencaAmbientalRepository.java
    │   │   │   ├── AuditoriaAmbientalRepository.java
    │   │   │   ├── EmissaoCarbonoRepository.java
    │   │   │   ├── CompensacaoAmbientalRepository.java
    │   │   │   ├── AgendamentoReducaoCarbonoRepository.java
    │   │   │   └── UsuarioRepository.java
    │   │   ├── entity/
    │   │   │   ├── Empresa.java
    │   │   │   ├── LicencaAmbiental.java
    │   │   │   ├── AuditoriaAmbiental.java
    │   │   │   ├── EmissaoCarbono.java
    │   │   │   ├── CompensacaoAmbiental.java
    │   │   │   ├── AgendamentoReducaoCarbono.java
    │   │   │   ├── Usuario.java
    │   │   │   ├── StatusLicenca.java
    │   │   │   ├── StatusAgendamento.java
    │   │   │   ├── NivelRisco.java
    │   │   │   └── Role.java
    │   │   ├── dto/
    │   │   │   ├── EmpresaRequestDTO.java / EmpresaResponseDTO.java
    │   │   │   ├── LicencaRequestDTO.java / LicencaResponseDTO.java
    │   │   │   ├── AuditoriaRequestDTO.java / AuditoriaResponseDTO.java
    │   │   │   ├── EmissaoRequestDTO.java / EmissaoResponseDTO.java
    │   │   │   ├── CompensacaoRequestDTO.java / CompensacaoResponseDTO.java
    │   │   │   ├── AgendamentoRequestDTO.java / AgendamentoResponseDTO.java
    │   │   │   ├── IndicadoresSustentabilidadeDTO.java
    │   │   │   ├── RegisterRequestDTO.java / LoginRequestDTO.java / AuthResponseDTO.java
    │   │   │   └── RelatorioEmpresaDTO.java
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── BusinessException.java
    │   │   │   ├── EmailAlreadyExistsException.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── DataInitializer.java
    │   │   └── security/
    │   │       ├── JwtService.java
    │   │       └── JwtAuthenticationFilter.java
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/
    │           ├── V1__create_tables.sql
    │           ├── V2__insert_initial_data.sql
    │           └── V3__create_agendamento_reducao_carbono.sql
    └── test/
        └── java/br/com/fiap/ecocompliance/
            ├── controller/
            │   ├── EmpresaControllerTest.java
            │   ├── EmissaoControllerTest.java
            │   ├── AgendamentoControllerTest.java
            │   ├── AuditoriaControllerTest.java
            │   ├── CompensacaoControllerTest.java
            │   └── LicencaControllerTest.java
            └── service/
                ├── EmpresaServiceTest.java
                ├── EmissaoServiceTest.java
                ├── AgendamentoServiceTest.java
                ├── AuditoriaServiceTest.java
                ├── CompensacaoServiceTest.java
                ├── LicencaServiceTest.java
                ├── IndicadoresServiceTest.java
                └── RelatorioServiceTest.java
```

---

## Boas práticas aplicadas

- **DTOs como `Record`** (imutáveis) — entidades nunca expostas via REST.
- **Bean Validation** em todos os DTOs de entrada (`@NotBlank`, `@NotNull`, `@Email`, `@Positive`, `@Future`, `@FutureOrPresent`, `@PastOrPresent`, `@Size`, `@Pattern`).
- **Paginação** nos endpoints de listagem — `Page<T>` com `@PageableDefault`, sem alteração de migrations.
- **`@RestControllerAdvice`** centralizando o tratamento de erros e padronizando o JSON de resposta.
- **Regras de negócio no service** — validação de datas, bloqueio de edição por status.
- **Códigos HTTP semânticos** (200/201/204/400/401/403/404/409).
- **JWT stateless** com Spring Security e filtro `OncePerRequestFilter`.
- **RBAC** (Role-Based Access Control) — ADMIN para escrita, USER para leitura.
- **Senhas em BCrypt** geradas pelo `PasswordEncoder` (nunca em SQL).
- **Flyway** para versionamento de schema (V1 tabelas, V2 dados, V3 agendamentos).
- **Lombok 1.18.38** compatível com JDK 21–24.
- **SpringDoc OpenAPI** com esquema de segurança Bearer JWT documentado.
- **111 testes** (63 de service + 45 de controller, Mockito puro + `@WebMvcTest`) — 0 falhas.
- **`spring.jpa.open-in-view=false`** evita lazy loading fora da transação.
- **Docker multi-stage** para imagem final pequena (~250 MB com JRE Alpine).
- **Healthcheck** no `docker-compose` garante que a API só sobe após o Oracle estar pronto.

---

## Autores

**Évelyn Santos Rodrigues** — RM 565919,
**Fagner Nunes Lopes** — RM 564220,
**Lucas Fernando da Silva** — RM 563626,
**Raphael Salomão Chamma** — RM 564765,
**Ricardo Negrão da Veiga** — RM 564106.

Atividade: Java Advanced – Microsserviços com Spring (Capítulo 8).
