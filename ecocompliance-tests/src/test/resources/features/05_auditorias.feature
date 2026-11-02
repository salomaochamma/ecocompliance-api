# language: pt
@auditorias @ESG-Governanca
Funcionalidade: Auditorias ambientais com classificação de risco
  Como auditor interno de meio ambiente
  Quero registrar auditorias classificadas por nível de risco
  Para priorizar planos de ação e manter a rastreabilidade das não conformidades

  Contexto:
    Dado que estou autenticado como "ADMIN"
    E que existe uma empresa cadastrada

  @positivo @smoke @contrato
  Cenário: Registrar auditoria ambiental
    Quando envio uma requisição POST para "/auditorias" com o corpo:
      | empresaId     | {empresa.id}                            |
      | dataAuditoria | {hoje-7}                                |
      | resultado     | Conformidade parcial                    |
      | observacoes   | Não conformidades na gestão de resíduos |
      | nivelRisco    | MEDIO                                   |
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "auditoria"
    E o campo "empresaNome" deve ser "{empresa.nome}"
    E o campo "dataAuditoria" deve ser "{hoje-7}"
    E o campo "nivelRisco" deve ser "MEDIO"

  @positivo @contrato
  Cenário: Listar auditorias
    Dado que existe uma auditoria com risco "BAIXO" para essa empresa
    Quando envio uma requisição GET para "/auditorias"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auditoria-lista"
    E a lista deve conter um item com "id" igual a "{auditoria.id}"

  @positivo @contrato
  Cenário: Consultar auditoria por ID
    Dado que existe uma auditoria com risco "MEDIO" para essa empresa
    Quando envio uma requisição GET para "/auditorias/{auditoria.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auditoria"
    E o campo "id" deve ser "{auditoria.id}"
    E o campo "nivelRisco" deve ser "MEDIO"

  @positivo @contrato
  Cenário: Filtrar auditorias de alto risco
    Dado que existe uma auditoria com risco "ALTO" para essa empresa
    Quando envio uma requisição GET para "/auditorias/risco/ALTO"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auditoria-lista"
    E a lista deve conter um item com "id" igual a "{auditoria.id}"
    E todos os itens da lista devem ter "nivelRisco" igual a "ALTO"

  @positivo @contrato
  Cenário: Reclassificar o risco após o plano de ação
    Dado que existe uma auditoria com risco "ALTO" para essa empresa
    Quando envio uma requisição PUT para "/auditorias/{auditoria.id}" com o corpo:
      | empresaId     | {empresa.id}            |
      | dataAuditoria | {hoje-1}                |
      | resultado     | Conformidade total      |
      | observacoes   | Plano de ação concluído |
      | nivelRisco    | BAIXO                   |
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "auditoria"
    E o campo "resultado" deve ser "Conformidade total"
    E o campo "nivelRisco" deve ser "BAIXO"

  @positivo
  Cenário: Excluir uma auditoria
    Dado que existe uma auditoria com risco "BAIXO" para essa empresa
    Quando envio uma requisição DELETE para "/auditorias/{auditoria.id}"
    Então o status da resposta deve ser 204
    E a resposta não deve ter corpo
    E ao consultar "/auditorias/{auditoria.id}" o status deve ser 404

  @negativo @contrato
  Cenário: Auditoria com data futura é rejeitada
    Quando envio uma requisição POST para "/auditorias" com o corpo:
      | empresaId     | {empresa.id}         |
      | dataAuditoria | {hoje+30}            |
      | resultado     | Auditoria antecipada |
      | observacoes   | [nulo]               |
      | nivelRisco    | BAIXO                |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de validação do campo "dataAuditoria" deve ser "A data da auditoria deve ser passada ou presente"

  @negativo @contrato
  Cenário: Auditoria sem nível de risco é rejeitada
    Quando envio uma requisição POST para "/auditorias" com o corpo:
      | empresaId     | {empresa.id}      |
      | dataAuditoria | {hoje-2}          |
      | resultado     | Sem classificação |
      | nivelRisco    | [nulo]            |
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro"
    E a mensagem de validação do campo "nivelRisco" deve ser "O nível de risco é obrigatório"

  @negativo @contrato
  Cenário: Consultar auditoria inexistente
    Quando envio uma requisição GET para "/auditorias/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Auditoria não encontrada com ID: 999999999"

  @negativo @contrato
  Cenário: Excluir auditoria inexistente
    Quando envio uma requisição DELETE para "/auditorias/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Auditoria não encontrada com ID: 999999999"
