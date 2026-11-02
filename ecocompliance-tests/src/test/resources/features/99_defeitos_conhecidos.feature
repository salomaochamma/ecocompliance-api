# language: pt
@defeito
Funcionalidade: Defeitos conhecidos (cenários em quarentena)
  Divergências encontradas durante a automação dos testes.
  Estes cenários descrevem o comportamento ESPERADO e hoje falham de propósito.
  Ficam fora da execução padrão (filtro "not @defeito") e passam a valer
  como testes de regressão assim que a correção for aplicada na API.

  @DEF-01 @seguranca
  Cenário: Requisição sem token deveria responder 401 Unauthorized
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/empresas"
    Então o status da resposta deve ser 401

  @DEF-02 @seguranca
  Cenário: Nível de risco inválido na URL deveria responder 400 sem expor detalhes internos
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição GET para "/auditorias/risco/CRITICO"
    Então o status da resposta deve ser 400
    E a resposta não deve expor detalhes internos da aplicação

  @DEF-03
  Cenário: Corpo JSON malformado deveria responder 400 Bad Request
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição POST para "/empresas" com o corpo bruto:
      """
      { "nome": "Empresa sem fechar a chave"
      """
    Então o status da resposta deve ser 400

  @DEF-04 @ESG-Social
  Cenário: Mensagens de erro deveriam estar escritas corretamente
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição GET para "/licencas/999999999"
    Então o status da resposta deve ser 404
    E a mensagem de erro deve conter "Licença não encontrada com ID: 999999999"
