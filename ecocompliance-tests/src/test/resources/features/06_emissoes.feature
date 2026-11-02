# language: pt
@emissoes @ESG-Ambiental
Funcionalidade: Inventário de emissões de carbono
  Como analista de sustentabilidade
  Quero registrar as emissões de gases de efeito estufa por fonte e período
  Para acompanhar a pegada de carbono da empresa nos Escopos 1, 2 e 3

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe uma empresa cadastrada

  @positivo @smoke @contrato
  Cenário: Registrar emissão de carbono
    Quando envio uma requisição POST para "/emissoes" com o corpo:
      | empresaId     | {empresa.id}                    |
      | quantidadeCo2 | 750.5                           |
      | fonteEmissao  | Combustão industrial (Escopo 1) |
      | dataRegistro  | {hoje-3}                        |
      | unidadeMedida | tCO2e                           |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "emissao"
    E o campo "empresaId" deve ser "{empresa.id}"
    E o campo "quantidadeCo2" deve ser o número "750.5"
    E o campo "unidadeMedida" deve ser "tCO2e"

  @positivo @contrato
  Cenário: Listar emissões
    Dado que existe uma emissão de "320.25" tCO2e para essa empresa
    Quando envio uma requisição GET para "/emissoes"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "emissao-lista"
    E a lista deve conter um item com "id" igual a "{emissao.id}"

  @positivo @contrato
  Cenário: Consultar emissão por ID
    Dado que existe uma emissão de "980.75" tCO2e para essa empresa
    Quando envio uma requisição GET para "/emissoes/{emissao.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "emissao"
    E o campo "id" deve ser "{emissao.id}"
    E o campo "quantidadeCo2" deve ser o número "980.75"

  @positivo @contrato
  Cenário: Consultar o inventário de emissões de uma empresa
    Dado que existe uma emissão de "120.25" tCO2e para essa empresa
    E que existe uma emissão de "79.75" tCO2e para essa empresa
    Quando envio uma requisição GET para "/emissoes/empresa/{empresa.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "emissao-lista"
    E a lista deve conter 2 itens
    E todos os itens da lista devem ter "empresaId" igual a "{empresa.id}"

  @positivo @contrato
  Cenário: Corrigir o valor de uma emissão registrada
    Dado que existe uma emissão de "100" tCO2e para essa empresa
    Quando envio uma requisição PUT para "/emissoes/{emissao.id}" com o corpo:
      | empresaId     | {empresa.id}               |
      | quantidadeCo2 | 95.4                       |
      | fonteEmissao  | Frota de veículos revisada |
      | dataRegistro  | {hoje-20}                  |
      | unidadeMedida | tCO2e                      |
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "emissao"
    E o campo "quantidadeCo2" deve ser o número "95.4"
    E o campo "fonteEmissao" deve ser "Frota de veículos revisada"

  @positivo
  Cenário: Excluir uma emissão
    Dado que existe uma emissão de "10" tCO2e para essa empresa
    Quando envio uma requisição DELETE para "/emissoes/{emissao.id}"
    Então o status da resposta deve ser 204
    E a resposta não deve ter corpo
    E ao consultar "/emissoes/{emissao.id}" o status deve ser 404

  @negativo @contrato
  Cenário: Emissão com quantidade negativa é rejeitada
    Quando envio uma requisição POST para "/emissoes" com o corpo:
      | empresaId     | {empresa.id} |
      | quantidadeCo2 | -50          |
      | fonteEmissao  | Caldeira     |
      | dataRegistro  | {hoje-1}     |
      | unidadeMedida | tCO2e        |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de validação do campo "quantidadeCo2" deve ser "A quantidade de CO2 deve ser positiva"

  @negativo @contrato
  Cenário: Inventário de empresa inexistente
    Quando envio uma requisição GET para "/emissoes/empresa/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada"

  @negativo @contrato
  Cenário: Consultar emissão inexistente
    Quando envio uma requisição GET para "/emissoes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"

  @negativo @contrato
  Cenário: Excluir emissão inexistente
    Quando envio uma requisição DELETE para "/emissoes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"
