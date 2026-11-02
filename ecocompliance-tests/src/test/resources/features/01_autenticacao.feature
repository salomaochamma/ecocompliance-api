# language: pt
@autenticacao @ESG-Governanca
Funcionalidade: Autenticação de usuários com JWT
  Como gestor de compliance ambiental da empresa
  Quero que somente usuários com credenciais válidas recebam um token de acesso
  Para garantir a segurança e a rastreabilidade das informações ESG

  @positivo @smoke @contrato
  Cenário: Login do administrador com credenciais válidas
    Quando faço login com o e-mail "admin@ecocompliance.com" e a senha "admin123"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auth-response"
    E o campo "tipo" deve ser "Bearer"
    E o campo "email" deve ser "admin@ecocompliance.com"
    E o campo "role" deve ser "ADMIN"
    E o campo "token" não deve estar vazio
    E a resposta não deve expor o campo "senha"

  @positivo @contrato @ESG-Social
  Cenário: Login de usuário com perfil somente leitura
    Quando faço login com o e-mail "user@ecocompliance.com" e a senha "user123"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auth-response"
    E o campo "email" deve ser "user@ecocompliance.com"
    E o campo "role" deve ser "USER"

  @negativo @contrato
  Cenário: Login com senha incorreta é rejeitado
    Quando faço login com o e-mail "admin@ecocompliance.com" e a senha "senha-errada"
    Então o status da resposta deve ser 401
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Credenciais inválidas"

  @negativo @seguranca
  Cenário: Login com e-mail inexistente não revela se o usuário existe
    Quando faço login com o e-mail "ninguem@ecocompliance.com" e a senha "qualquer123"
    Então o status da resposta deve ser 401
    E a mensagem de erro deve conter "Credenciais inválidas"

  @negativo @contrato
  Cenário: Login sem e-mail retorna erro de validação
    Quando envio uma requisição POST para "/auth/login" com o corpo:
      | email | [vazio]  |
      | senha | admin123 |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de validação do campo "email" deve ser "O e-mail é obrigatório"

  @positivo @contrato
  Cenário: Cadastro de novo usuário com perfil de consulta
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      | nome  | Analista ESG QA |
      | email | {emailUnico}    |
      | senha | senhaSegura123  |
      | role  | USER            |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "auth-response"
    E o campo "nome" deve ser "Analista ESG QA"
    E o campo "email" deve ser "{emailUnico}"
    E o campo "role" deve ser "USER"
    E a resposta não deve expor o campo "senha"

  @positivo
  Cenário: Usuário recém-cadastrado consegue se autenticar
    Dado que existe um usuário cadastrado com o e-mail "{emailUnico}", senha "senhaSegura123" e perfil "USER"
    Quando faço login com o e-mail "{emailUnico}" e a senha "senhaSegura123"
    Então o status da resposta deve ser 200
    E o campo "email" deve ser "{emailUnico}"
    E o campo "role" deve ser "USER"

  @negativo @contrato
  Cenário: Cadastro com e-mail já utilizado é rejeitado
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      | nome  | Administrador Duplicado |
      | email | admin@ecocompliance.com |
      | senha | outraSenha123           |
      | role  | USER                    |
    Então o status da resposta deve ser 409
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "E-mail já cadastrado"

  @negativo @contrato @ESG-Social
  Cenário: Cadastro com dados inválidos informa o erro de cada campo
    Quando envio uma requisição POST para "/auth/register" com o corpo:
      | nome  | A              |
      | email | email-invalido |
      | senha | 123            |
      | role  | USER           |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E deve haver erro de validação no campo "nome"
    E a mensagem de validação do campo "email" deve ser "E-mail inválido"
    E a mensagem de validação do campo "senha" deve ser "A senha deve ter entre 6 e 100 caracteres"
