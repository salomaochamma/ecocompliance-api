# language: pt
@compensacoes @ESG-Ambiental
Funcionalidade: Ações de compensação ambiental
  Como gestor de sustentabilidade
  Quero documentar as ações de compensação ambiental realizadas
  Para comprovar a neutralização de parte das emissões da empresa

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe uma empresa cadastrada

  @positivo @smoke @contrato
  Cenário: Registrar ação de reflorestamento
    Quando envio uma requisição POST para "/compensacoes" com o corpo:
      | empresaId            | {empresa.id}                              |
      | tipoCompensacao      | Reflorestamento                           |
      | descricao            | Plantio de 10.000 mudas em área degradada |
      | quantidadeCompensada | 3000                                      |
      | dataAcao             | {hoje-12}                                 |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "compensacao"
    E o campo "empresaNome" deve ser "{empresa.nome}"
    E o campo "tipoCompensacao" deve ser "Reflorestamento"
    E o campo "quantidadeCompensada" deve ser o número "3000"

  @positivo @contrato
  Cenário: Listar compensações
    Dado que existe uma compensação de "500" tCO2e para essa empresa
    Quando envio uma requisição GET para "/compensacoes"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "compensacao-lista"
    E a lista deve conter um item com "id" igual a "{compensacao.id}"

  @positivo @contrato
  Cenário: Consultar compensação por ID
    Dado que existe uma compensação de "1500.5" tCO2e para essa empresa
    Quando envio uma requisição GET para "/compensacoes/{compensacao.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "compensacao"
    E o campo "id" deve ser "{compensacao.id}"
    E o campo "quantidadeCompensada" deve ser o número "1500.5"

  @positivo @contrato
  Cenário: Atualizar uma ação de compensação
    Dado que existe uma compensação de "200" tCO2e para essa empresa
    Quando envio uma requisição PUT para "/compensacoes/{compensacao.id}" com o corpo:
      | empresaId            | {empresa.id}                                  |
      | tipoCompensacao      | Créditos de Carbono                           |
      | descricao            | Créditos certificados (VCS) de projeto eólico |
      | quantidadeCompensada | 250.75                                        |
      | dataAcao             | {hoje-5}                                      |
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "compensacao"
    E o campo "tipoCompensacao" deve ser "Créditos de Carbono"
    E o campo "quantidadeCompensada" deve ser o número "250.75"

  @positivo
  Cenário: Excluir uma ação de compensação
    Dado que existe uma compensação de "50" tCO2e para essa empresa
    Quando envio uma requisição DELETE para "/compensacoes/{compensacao.id}"
    Então o status da resposta deve ser 204
    E a resposta não deve ter corpo
    E ao consultar "/compensacoes/{compensacao.id}" o status deve ser 404

  @negativo @contrato
  Cenário: Compensação com quantidade zero é rejeitada
    Quando envio uma requisição POST para "/compensacoes" com o corpo:
      | empresaId            | {empresa.id}        |
      | tipoCompensacao      | Energia Renovável   |
      | descricao            | Ação sem quantidade |
      | quantidadeCompensada | 0                   |
      | dataAcao             | {hoje-1}            |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de validação do campo "quantidadeCompensada" deve ser "A quantidade compensada deve ser positiva"

  @negativo @contrato
  Cenário: Compensação para empresa inexistente é rejeitada
    Quando envio uma requisição POST para "/compensacoes" com o corpo:
      | empresaId            | 999999999       |
      | tipoCompensacao      | Reflorestamento |
      | descricao            | Empresa fantasma |
      | quantidadeCompensada | 10              |
      | dataAcao             | {hoje-1}        |
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada"

  @negativo @contrato
  Cenário: Consultar compensação inexistente
    Quando envio uma requisição GET para "/compensacoes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"

  @negativo @contrato
  Cenário: Excluir compensação inexistente
    Quando envio uma requisição DELETE para "/compensacoes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "não encontrada com ID: 999999999"
