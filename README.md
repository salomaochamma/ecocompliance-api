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
- [Usuários iniciais](#usuários-iniciais)
- [Endpoints disponíveis](#endpoints-disponíveis)
- [Exemplos de JSON para testar](#exemplos-de-json-para-testar)
- [Postman Collection](#postman-collection)
- [Estrutura de pacotes](#estrutura-de-pacotes)

---

## Tema ESG

**Governança e Compliance Ambiental.**

A **EcoCompliance API** apoia empresas na gestão de sua conformidade ambiental, oferecendo um cadastro centralizado para:

- Empresas e seus setores de atuação
- Licenças ambientais e seus prazos de validade
- Auditorias internas com classificação de risco
- Inventário de emissões de carbono (Escopo 1, 2 e 3)
- Ações de compensação ambiental (reflorestamento, créditos de carbono, energia renovável)

A API gera ainda um **relatório consolidado por empresa** com KPIs ESG: emissões totais, carbono compensado, licenças ativas/vencidas, número de auditorias e maior nível de risco identificado — dados úteis para reports ESG (GRI, SASB, TCFD).

---

## Objetivo do sistema

Permitir que uma organização:

1. Mantenha um inventário confiável de **licenças ambientais** com alerta de vencimento.
2. Registre **auditorias** classificadas por nível de risco (BAIXO, MEDIO, ALTO).
3. Acompanhe suas **emissões de carbono** por fonte e período.
4. Documente as **compensações ambientais** realizadas.
5. Gere um **relatório ESG** consolidado por empresa.

---

## Tecnologias

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | **Java 21** |
| Framework | **Spring Boot 3.3.x** |
| Build | **Maven** |
| Web | Spring Web (REST) |
| Persistência | Spring Data JPA + Hibernate |
| Validação | Spring Validation (Jakarta Bean Validation) |
| Segurança | Spring Security + **JWT (jjwt 0.12)** |
| Banco | **Oracle Database** (driver `ojdbc11`) |
| Migrações | **Flyway** (`flyway-core` + `flyway-database-oracle`) |
| Boilerplate | **Lombok** |
| Container | **Docker** + **docker-compose** |
| Testes | Spring Boot Test, Spring Security Test |
| Testes manuais | Coleção **Postman** (importável no Insomnia) |

---

## Arquitetura em camadas

```
controller   → recebe requisições HTTP, valida, delega
service      → regras de negócio, transações
repository   → acesso a dados (Spring Data JPA)
entity       → modelo de domínio (JPA)
dto          → objetos de entrada/saída (Records)
exception    → exceções customizadas + GlobalExceptionHandler
config       → SecurityConfig, DataInitializer
security     → JwtService, JwtAuthenticationFilter
```

> **Princípio aplicado:** os controllers nunca expõem entidades JPA. Toda E/S passa por DTOs (`Record`), validados com Bean Validation.

---

## Como rodar

### Pré-requisitos

- **JDK 21** 
- **Maven 3.9+** 
- **Acesso a um Oracle Database** (FIAP, Oracle XE local, ou via docker-compose)
- (Opcional) **Docker Desktop** para rodar com containers

### Configurar Oracle (FIAP ou local)

A API usa as variáveis de ambiente abaixo (com defaults para FIAP em `application.properties`):

| Variável | Default | Descrição                             |
|----------|---------|---------------------------------------|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` | JDBC URL do Oracle                    |
| `DB_USERNAME` | `rm564765` | Usuário Oracle (usando RM da FIAP)    |
| `DB_PASSWORD` | `senha` | Senha do banco                        |
| `JWT_SECRET` | chave Base64 inclusa | Chave HMAC-SHA para assinar os tokens |

Edite `src/main/resources/application.properties` com user/senha **ou** exporte as variáveis antes de rodar:

```bash
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USERNAME="rm564765"
export DB_PASSWORD="senha"
```

> O **Flyway** cria automaticamente todas as tabelas e popula dados iniciais (empresas, licenças, auditorias, emissões e compensações) na primeira execução. Os usuários `admin` e `user` são criados pela classe `DataInitializer` com BCrypt gerado pelo Spring Security.

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

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/empresas` | USER, ADMIN |
| `GET` | `/empresas/{id}` | USER, ADMIN |
| `POST` | `/empresas` | ADMIN |
| `PUT` | `/empresas/{id}` | ADMIN |
| `DELETE` | `/empresas/{id}` | ADMIN |

### Licenças ambientais

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/licencas` | USER, ADMIN |
| `GET` | `/licencas/{id}` | USER, ADMIN |
| `GET` | `/licencas/vencendo` | USER, ADMIN |
| `POST` | `/licencas` | ADMIN |
| `PUT` | `/licencas/{id}` | ADMIN |
| `DELETE` | `/licencas/{id}` | ADMIN |

### Auditorias

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/auditorias` | USER, ADMIN |
| `GET` | `/auditorias/{id}` | USER, ADMIN |
| `GET` | `/auditorias/risco/{nivelRisco}` | USER, ADMIN |
| `POST` | `/auditorias` | ADMIN |
| `PUT` | `/auditorias/{id}` | ADMIN |
| `DELETE` | `/auditorias/{id}` | ADMIN |

### Emissões de carbono

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/emissoes` | USER, ADMIN |
| `GET` | `/emissoes/{id}` | USER, ADMIN |
| `GET` | `/emissoes/empresa/{empresaId}` | USER, ADMIN |
| `POST` | `/emissoes` | ADMIN |
| `PUT` | `/emissoes/{id}` | ADMIN |
| `DELETE` | `/emissoes/{id}` | ADMIN |

### Compensações ambientais

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/compensacoes` | USER, ADMIN |
| `GET` | `/compensacoes/{id}` | USER, ADMIN |
| `POST` | `/compensacoes` | ADMIN |
| `PUT` | `/compensacoes/{id}` | ADMIN |
| `DELETE` | `/compensacoes/{id}` | ADMIN |

### Relatórios ESG

| Método | Rota | Roles |
|--------|------|-------|
| `GET` | `/relatorios/empresa/{empresaId}` | USER, ADMIN |

### Códigos HTTP retornados

| Status | Significado |
|--------|-------------|
| `200 OK` | Consulta/atualização bem-sucedida |
| `201 Created` | Recurso criado (POST) |
| `204 No Content` | Exclusão bem-sucedida (DELETE) |
| `400 Bad Request` | Erro de validação ou regra de negócio |
| `401 Unauthorized` | Token ausente, inválido ou expirado |
| `403 Forbidden` | Usuário autenticado mas sem permissão (USER tentando POST/PUT/DELETE) |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Recurso duplicado (CNPJ, e-mail, número de licença) |

---

## Exemplos de JSON para testar

### Registrar usuário

```http
POST /auth/register
Content-Type: application/json

{
  "nome": "Novo Admin",
  "email": "novoadmin@ecocompliance.com",
  "senha": "admin123",
  "role": "ADMIN"
}
```

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

### Criar auditoria

```http
POST /auditorias
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "dataAuditoria": "2025-04-15",
  "resultado": "Conformidade total",
  "observacoes": "Atende ISO 14001",
  "nivelRisco": "BAIXO"
}
```

> `nivelRisco` aceita: `BAIXO`, `MEDIO`, `ALTO`.

### Criar emissão de carbono

```http
POST /emissoes
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "quantidadeCo2": 750.5000,
  "fonteEmissao": "Combustão industrial",
  "dataRegistro": "2025-04-30",
  "unidadeMedida": "tCO2e"
}
```

### Criar compensação ambiental

```http
POST /compensacoes
Authorization: Bearer <token>
Content-Type: application/json

{
  "empresaId": 1,
  "tipoCompensacao": "Reflorestamento",
  "descricao": "Plantio de 10.000 mudas em área degradada",
  "quantidadeCompensada": 3000.0000,
  "dataAcao": "2025-04-20"
}
```

### Relatório ESG da empresa

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
  "path": "/empresas",
  "fieldErrors": [
    { "campo": "cnpj", "mensagem": "CNPJ inválido. Use o formato 00.000.000/0000-00 ou apenas dígitos" },
    { "campo": "emailResponsavel", "mensagem": "E-mail inválido" }
  ]
}
```

Recurso não encontrado:

```json
{
  "timestamp": "2025-04-30T10:15:30.123",
  "status": 404,
  "error": "Not Found",
  "message": "Empresa não encontrada com ID: 999",
  "path": "/empresas/999",
  "fieldErrors": null
}
```

---

## Postman Collection

A coleção está em `postman/EcoCompliance-API.postman_collection.json`.

### Importar no Postman

1. Postman → **Import** → arraste o arquivo `.json`.
2. Pastas: **Auth**, **Empresas**, **Licenças**, **Auditorias**, **Emissões**, **Compensações**, **Relatórios**.
3. Faça **Login (Admin)** primeiro — o token é salvo automaticamente na variável `{{token}}` por um script de teste.
4. As demais requisições usam o token via Bearer Auth herdado da coleção.

### Importar no Insomnia

1. Insomnia → **Create / Import** → **From File** → selecione o `.json`.
2. O Insomnia entende formato Postman v2.1.

> Variável `baseUrl` = `http://localhost:8080`. Ajuste se necessário.

---

## Estrutura de pacotes

```
ecocompliance/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
├── postman/
│   └── EcoCompliance-API.postman_collection.json
└── src/
    └── main/
        ├── java/br/com/fiap/ecocompliance/
        │   ├── EcoComplianceApplication.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── EmpresaController.java
        │   │   ├── LicencaController.java
        │   │   ├── AuditoriaController.java
        │   │   ├── EmissaoController.java
        │   │   ├── CompensacaoController.java
        │   │   └── RelatorioController.java
        │   ├── service/
        │   │   ├── AuthService.java
        │   │   ├── EmpresaService.java
        │   │   ├── LicencaService.java
        │   │   ├── AuditoriaService.java
        │   │   ├── EmissaoService.java
        │   │   ├── CompensacaoService.java
        │   │   └── RelatorioService.java
        │   ├── repository/
        │   │   ├── EmpresaRepository.java
        │   │   ├── LicencaAmbientalRepository.java
        │   │   ├── AuditoriaAmbientalRepository.java
        │   │   ├── EmissaoCarbonoRepository.java
        │   │   ├── CompensacaoAmbientalRepository.java
        │   │   └── UsuarioRepository.java
        │   ├── entity/
        │   │   ├── Empresa.java
        │   │   ├── LicencaAmbiental.java
        │   │   ├── AuditoriaAmbiental.java
        │   │   ├── EmissaoCarbono.java
        │   │   ├── CompensacaoAmbiental.java
        │   │   ├── Usuario.java
        │   │   ├── StatusLicenca.java
        │   │   ├── NivelRisco.java
        │   │   └── Role.java
        │   ├── dto/
        │   │   ├── EmpresaRequestDTO.java / EmpresaResponseDTO.java
        │   │   ├── LicencaRequestDTO.java / LicencaResponseDTO.java
        │   │   ├── AuditoriaRequestDTO.java / AuditoriaResponseDTO.java
        │   │   ├── EmissaoRequestDTO.java / EmissaoResponseDTO.java
        │   │   ├── CompensacaoRequestDTO.java / CompensacaoResponseDTO.java
        │   │   ├── RegisterRequestDTO.java / LoginRequestDTO.java / AuthResponseDTO.java
        │   │   └── RelatorioEmpresaDTO.java
        │   ├── exception/
        │   │   ├── ResourceNotFoundException.java
        │   │   ├── BusinessException.java
        │   │   ├── EmailAlreadyExistsException.java
        │   │   ├── ErrorResponse.java
        │   │   └── GlobalExceptionHandler.java
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   └── DataInitializer.java
        │   └── security/
        │       ├── JwtService.java
        │       └── JwtAuthenticationFilter.java
        └── resources/
            ├── application.properties
            └── db/migration/
                ├── V1__create_tables.sql
                └── V2__insert_initial_data.sql
```

---

## Boas práticas aplicadas

- **DTOs como `Record`** (imutáveis) — entidades nunca expostas via REST.
- **Bean Validation** em todos os DTOs de entrada (`@NotBlank`, `@NotNull`, `@Email`, `@Positive`, `@PastOrPresent`, `@Size`, `@Pattern`).
- **`@RestControllerAdvice`** centralizando o tratamento de erros e padronizando o JSON de resposta.
- **Códigos HTTP semânticos** (200/201/204/400/401/403/404/409).
- **JWT stateless** com Spring Security e filtro `OncePerRequestFilter`.
- **Senhas em BCrypt** geradas pelo `PasswordEncoder` (nunca em SQL).
- **Flyway** para versionamento de schema (V1 cria tabelas, V2 popula dados de teste).
- **Lombok** para reduzir boilerplate (`@Builder`, `@Getter`, `@Setter`, `@RequiredArgsConstructor`).
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
