# language: pt
@relatorios @ESG-Ambiental @ESG-Governanca
Funcionalidade: Relatório ESG consolidado por empresa
  Como diretor de sustentabilidade
  Quero um relatório com os indicadores ambientais consolidados de cada empresa
  Para apoiar os reports ESG (GRI, SASB, TCFD) com dados íntegros e rastreáveis

  Contexto:
    Dado que estou autenticado como "USER"
    E que existe uma empresa cadastrada

  @positivo @smoke @contrato @ESG-Eficiencia
  Cenário: Relatório consolida emissões, compensações, licenças e risco
    Dado que existe uma emissão de "100.5" tCO2e para essa empresa
    E que existe uma emissão de "49.5" tCO2e para essa empresa
    E que existe uma compensação de "40" tCO2e para essa empresa
    E que existe uma licença "ATIVA" com validade daqui a 200 dias para essa empresa
    E que existe uma licença "VENCIDA" com validade expirada há 10 dias para essa empresa
    E que existe uma auditoria com risco "BAIXO" para essa empresa
    E que existe uma auditoria com risco "ALTO" para essa empresa
    Quando envio uma requisição GET para "/relatorios/empresa/{empresa.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "relatorio"
    E o campo "empresa.id" deve ser "{empresa.id}"
    E o campo "totalEmissoesCarbono" deve ser o número "150"
    E o campo "totalCarbonoCompensado" deve ser o número "40"
    E o campo "quantidadeLicencasAtivas" deve ser "1"
    E o campo "quantidadeLicencasVencidas" deve ser "1"
    E o campo "quantidadeAuditorias" deve ser "2"
    E o campo "maiorNivelRisco" deve ser "ALTO"
    E o tempo de resposta deve ser inferior a 3000 ms

  @positivo @contrato
  Cenário: Relatório de empresa ainda sem registros ambientais
    Quando envio uma requisição GET para "/relatorios/empresa/{empresa.id}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "relatorio"
    E o campo "totalEmissoesCarbono" deve ser o número "0"
    E o campo "totalCarbonoCompensado" deve ser o número "0"
    E o campo "quantidadeLicencasAtivas" deve ser "0"
    E o campo "quantidadeLicencasVencidas" deve ser "0"
    E o campo "quantidadeAuditorias" deve ser "0"
    E o campo "maiorNivelRisco" deve ser nulo

  @negativo @contrato
  Cenário: Relatório de empresa inexistente
    Quando envio uma requisição GET para "/relatorios/empresa/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro"
    E a mensagem de erro deve conter "Empresa não encontrada"
