# language: pt
@licencas @ESG-Governanca
Funcionalidade: Gestão de licenças ambientais
  Como responsável pelo compliance regulatório
  Quero controlar as licenças ambientais e os seus prazos de validade
  Para evitar que a empresa opere com licença vencida e sofra sanções

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe uma empresa cadastrada

  @positivo @smoke @contrato
  Cenário: Registrar licença ambiental válida
    Quando envio uma requisição POST para "/licencas" com o corpo:
      | empresaId     | {empresa.id}         |
      | tipoLicenca   | Licença de Operação  |
      | numeroLicenca | {numeroLicencaUnico} |
      | dataEmissao   | {hoje-30}            |
      | dataValidade  | {hoje+700}           |
      | status        | ATIVA                |
      | orgaoEmissor  | CETESB               |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "licenca"
    E o campo "empresaId" deve ser "{empresa.id}"
    E o campo "empresaNome" deve ser "{empresa.nome}"
    E o campo "numeroLicenca" deve ser "{numeroLicencaUnico}"
    E o campo "dataValidade" deve ser "{hoje+700}"
    E o campo "status" deve ser "ATIVA"

  @positivo @contrato
  Cenário: Listar licenças
    Dado que existe uma licença "ATIVA" com validade daqui a 365 dias para essa empresa
    Quando envio uma requisição GET para "/licencas"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "licenca-lista"
    E a lista deve conter um item com "id" igual a "{licenca.id}"

  @positivo @contrato
  Cenário: Consultar licença por ID
    Dado que existe uma licença "ATIVA" com validade daqui a 365 dias para essa empresa
    Quando envio uma requisição GET para "/licencas/{licenca.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "licenca"
    E o campo "numeroLicenca" deve ser "{licenca.numero}"
    E o campo "empresaId" deve ser "{empresa.id}"

  @positivo @contrato @ESG-Ambiental
  Cenário: Alertar licenças ativas que vencem nos próximos 30 dias
    Dado que existe uma licença "ATIVA" com validade daqui a 15 dias para essa empresa, identificada como "licencaVencendo"
    E que existe uma licença "ATIVA" com validade daqui a 120 dias para essa empresa, identificada como "licencaEmDia"
    Quando envio uma requisição GET para "/licencas/vencendo"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "licenca-lista"
    E a lista deve conter um item com "numeroLicenca" igual a "{licencaVencendo.numero}"
    E a lista não deve conter item com "numeroLicenca" igual a "{licencaEmDia.numero}"
    E todos os itens da lista devem ter "status" igual a "ATIVA"

  @positivo @contrato
  Cenário: Suspender uma licença
    Dado que existe uma licença "ATIVA" com validade daqui a 365 dias para essa empresa
    Quando envio uma requisição PUT para "/licencas/{licenca.id}" com o corpo:
      | empresaId     | {empresa.id}        |
      | tipoLicenca   | Licença de Operação |
      | numeroLicenca | {licenca.numero}    |
      | dataEmissao   | {hoje-30}           |
      | dataValidade  | {hoje+365}          |
      | status        | SUSPENSA            |
      | orgaoEmissor  | IBAMA               |
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "licenca"
    E o campo "status" deve ser "SUSPENSA"
    E o campo "orgaoEmissor" deve ser "IBAMA"

  @positivo
  Cenário: Excluir uma licença
    Dado que existe uma licença "ATIVA" com validade daqui a 365 dias para essa empresa
    Quando envio uma requisição DELETE para "/licencas/{licenca.id}"
    Então o status da resposta deve ser 204
    E a resposta não deve ter corpo
    E ao consultar "/licencas/{licenca.id}" o status deve ser 404

  @negativo @contrato
  Cenário: Data de validade anterior à data de emissão é rejeitada
    Quando envio uma requisição POST para "/licencas" com o corpo:
      | empresaId     | {empresa.id}         |
      | tipoLicenca   | Licença Prévia       |
      | numeroLicenca | {numeroLicencaUnico} |
      | dataEmissao   | {hoje-10}            |
      | dataValidade  | {hoje-40}            |
      | status        | ATIVA                |
      | orgaoEmissor  | CETESB               |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Data de validade não pode ser anterior"

  @negativo @contrato
  Cenário: Número de licença duplicado é bloqueado
    Dado que existe uma licença "ATIVA" com validade daqui a 365 dias para essa empresa
    Quando envio uma requisição POST para "/licencas" com o corpo:
      | empresaId     | {empresa.id}        |
      | tipoLicenca   | Licença de Operação |
      | numeroLicenca | {licenca.numero}    |
      | dataEmissao   | {hoje-5}            |
      | dataValidade  | {hoje+300}          |
      | status        | ATIVA               |
      | orgaoEmissor  | CETESB              |
    Então o status da resposta deve ser 409
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "integridade"
    E a resposta não deve expor detalhes internos da aplicação

  @negativo @contrato
  Cenário: Licença para empresa inexistente é rejeitada
    Quando envio uma requisição POST para "/licencas" com o corpo:
      | empresaId     | 999999999            |
      | tipoLicenca   | Licença de Operação  |
      | numeroLicenca | {numeroLicencaUnico} |
      | dataEmissao   | {hoje-30}            |
      | dataValidade  | {hoje+365}           |
      | status        | ATIVA                |
      | orgaoEmissor  | CETESB               |
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada"

  @negativo @contrato
  Cenário: Licença sem os campos obrigatórios é rejeitada
    Quando envio uma requisição POST para "/licencas" com o corpo:
      | empresaId     | {empresa.id} |
      | tipoLicenca   | [vazio]      |
      | numeroLicenca | [vazio]      |
      | dataEmissao   | [nulo]       |
      | dataValidade  | [nulo]       |
      | status        | [nulo]       |
      | orgaoEmissor  | [vazio]      |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E deve haver erro de validação no campo "tipoLicenca"
    E deve haver erro de validação no campo "numeroLicenca"
    E deve haver erro de validação no campo "dataEmissao"
    E deve haver erro de validação no campo "dataValidade"
    E deve haver erro de validação no campo "status"
    E deve haver erro de validação no campo "orgaoEmissor"

  @negativo @contrato
  Cenário: Consultar licença inexistente
    Quando envio uma requisição GET para "/licencas/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"

  @negativo @contrato
  Cenário: Excluir licença inexistente
    Quando envio uma requisição DELETE para "/licencas/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"
