# language: pt
@empresas @ESG-Governanca
Funcionalidade: Cadastro de empresas monitoradas
  Como analista de compliance ambiental
  Quero manter o cadastro das empresas monitoradas
  Para centralizar as informações ESG de cada organização

  Contexto:
    Dado que estou autenticado como "ADMIN"

  @positivo @smoke @contrato
  Cenário: Cadastrar empresa com dados válidos
    Quando envio uma requisição POST para "/empresas" com o corpo:
      | nome             | Verde Energia QA Ltda      |
      | cnpj             | {cnpjUnico}                |
      | setor            | Energia Renovável          |
      | emailResponsavel | esg@verdeenergia-qa.com.br |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "empresa"
    E o campo "nome" deve ser "Verde Energia QA Ltda"
    E o campo "cnpj" deve ser "{cnpjUnico}"
    E o campo "setor" deve ser "Energia Renovável"
    E guardo o campo "id" da resposta como "novaEmpresa"
    E o cabeçalho "Location" deve terminar com "/empresas/{novaEmpresa}"
    E ao consultar "/empresas/{novaEmpresa}" o status deve ser 200

  @positivo @contrato @ESG-Eficiencia
  Cenário: Listar empresas cadastradas
    Dado que existe uma empresa cadastrada
    Quando envio uma requisição GET para "/empresas"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "empresa-lista"
    E a lista deve conter um item com "id" igual a "{empresa.id}"
    E o tempo de resposta deve ser inferior a 3000 ms

  @positivo @contrato
  Cenário: Consultar empresa por ID
    Dado que existe uma empresa cadastrada
    Quando envio uma requisição GET para "/empresas/{empresa.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "empresa"
    E o campo "id" deve ser "{empresa.id}"
    E o campo "cnpj" deve ser "{empresa.cnpj}"

  @positivo @contrato
  Cenário: Atualizar os dados de uma empresa
    Dado que existe uma empresa cadastrada
    Quando envio uma requisição PUT para "/empresas/{empresa.id}" com o corpo:
      | nome             | Empresa Atualizada QA S.A.      |
      | cnpj             | {empresa.cnpj}                  |
      | setor            | Logística Verde                 |
      | emailResponsavel | compliance@atualizada-qa.com.br |
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "empresa"
    E o campo "nome" deve ser "Empresa Atualizada QA S.A."
    E o campo "setor" deve ser "Logística Verde"
    E o campo "emailResponsavel" deve ser "compliance@atualizada-qa.com.br"

  @positivo
  Cenário: Excluir uma empresa
    Dado que existe uma empresa cadastrada
    Quando envio uma requisição DELETE para "/empresas/{empresa.id}"
    Então o status da resposta deve ser 204
    E a resposta não deve ter corpo
    E ao consultar "/empresas/{empresa.id}" o status deve ser 404

  @negativo @contrato
  Cenário: Consultar empresa inexistente
    Quando envio uma requisição GET para "/empresas/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada com ID: 999999999"
    E a resposta não deve expor detalhes internos da aplicação

  @negativo @contrato @ESG-Social
  Cenário: Cadastro com CNPJ e e-mail inválidos informa o erro de cada campo
    Quando envio uma requisição POST para "/empresas" com o corpo:
      | nome             | Empresa Com Dados Inválidos |
      | cnpj             | 123                         |
      | setor            | Indústria                   |
      | emailResponsavel | email-sem-arroba            |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E o campo "message" deve ser "Erro de validação nos campos"
    E a mensagem de validação do campo "cnpj" deve ser "CNPJ inválido. Use o formato 00.000.000/0000-00 ou apenas dígitos"
    E a mensagem de validação do campo "emailResponsavel" deve ser "E-mail inválido"

  @negativo @contrato
  Cenário: Não é permitido cadastrar duas empresas com o mesmo CNPJ
    Dado que existe uma empresa cadastrada
    Quando envio uma requisição POST para "/empresas" com o corpo:
      | nome             | Empresa Duplicada QA |
      | cnpj             | {empresa.cnpj}       |
      | setor            | Varejo               |
      | emailResponsavel | duplicada@qa.com.br  |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Já existe uma empresa cadastrada com o CNPJ"

  @negativo
  Cenário: Atualizar empresa inexistente
    Quando envio uma requisição PUT para "/empresas/999999999" com o corpo:
      | nome             | Empresa Fantasma   |
      | cnpj             | {cnpjUnico}        |
      | setor            | Serviços           |
      | emailResponsavel | fantasma@qa.com.br |
    Então o status da resposta deve ser 404
    E a mensagem de erro deve conter "Empresa não encontrada"

  @negativo @contrato
  Cenário: Excluir empresa inexistente
    Quando envio uma requisição DELETE para "/empresas/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada com ID: 999999999"
