# language: pt
@seguranca @ESG-Governanca
Funcionalidade: Controle de acesso por perfil
  Como responsável pela governança dos dados ambientais
  Quero que cada perfil acesse somente o que lhe é permitido
  Para garantir a integridade das informações e a segregação de funções

  @negativo
  Esquema do Cenário: Requisição sem token é bloqueada em <recurso>
    Dado que não estou autenticado
    Quando envio uma requisição GET para "<endpoint>"
    Então o acesso deve ser negado

    Exemplos:
      | recurso      | endpoint              |
      | empresas     | /empresas             |
      | licenças     | /licencas             |
      | auditorias   | /auditorias           |
      | emissões     | /emissoes             |
      | compensações | /compensacoes         |
      | relatórios   | /relatorios/empresa/1 |

  @negativo
  Cenário: Token inválido é rejeitado
    Dado que estou usando um token inválido
    Quando envio uma requisição GET para "/empresas"
    Então o acesso deve ser negado

  @negativo
  Cenário: Token forjado para se passar pelo administrador é rejeitado
    Dado que estou usando um token forjado para o e-mail "admin@ecocompliance.com"
    Quando envio uma requisição GET para "/empresas"
    Então o acesso deve ser negado

  @positivo @contrato @ESG-Social
  Cenário: Perfil USER pode consultar os dados ambientais
    Dado que estou autenticado como "USER"
    Quando envio uma requisição GET para "/empresas"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "empresa-lista"

  @negativo
  Esquema do Cenário: Perfil USER não pode cadastrar nem alterar dados (<metodo> <endpoint>)
    Dado que estou autenticado como "USER"
    Quando envio uma requisição <metodo> para "<endpoint>" com o corpo:
      | nome | Tentativa sem permissão |
    Então o status da resposta deve ser 403

    Exemplos:
      | metodo | endpoint                |
      | POST   | /empresas               |
      | PUT    | /empresas/999999999     |
      | POST   | /licencas               |
      | PUT    | /licencas/999999999     |
      | POST   | /auditorias             |
      | PUT    | /auditorias/999999999   |
      | POST   | /emissoes               |
      | PUT    | /emissoes/999999999     |
      | POST   | /compensacoes           |
      | PUT    | /compensacoes/999999999 |

  @negativo
  Esquema do Cenário: Perfil USER não pode excluir registros (<endpoint>)
    Dado que estou autenticado como "USER"
    Quando envio uma requisição DELETE para "<endpoint>"
    Então o status da resposta deve ser 403

    Exemplos:
      | endpoint                |
      | /empresas/999999999     |
      | /licencas/999999999     |
      | /auditorias/999999999   |
      | /emissoes/999999999     |
      | /compensacoes/999999999 |
