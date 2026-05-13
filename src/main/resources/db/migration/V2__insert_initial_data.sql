-- =========================================================
-- =========================================================
-- =========================================================

-- ---------------------------------------------------------
-- EMPRESAS
-- ---------------------------------------------------------
INSERT INTO TB_EMPRESA (NM_EMPRESA, NR_CNPJ, DS_SETOR, DS_EMAIL_RESPONSAVEL)
VALUES ('Verde Industria S.A.', '12.345.678/0001-90', 'Manufatura', 'sustentabilidade@verdeindustria.com.br');

INSERT INTO TB_EMPRESA (NM_EMPRESA, NR_CNPJ, DS_SETOR, DS_EMAIL_RESPONSAVEL)
VALUES ('EcoEnergia Renovaveis Ltda', '23.456.789/0001-01', 'Energia', 'esg@ecoenergia.com.br');

INSERT INTO TB_EMPRESA (NM_EMPRESA, NR_CNPJ, DS_SETOR, DS_EMAIL_RESPONSAVEL)
VALUES ('Logistica Sustentavel Brasil', '34.567.890/0001-12', 'Transporte', 'compliance@logsus.com.br');

-- ---------------------------------------------------------
-- LICENCAS AMBIENTAIS
-- ---------------------------------------------------------
INSERT INTO TB_LICENCA_AMBIENTAL
(ID_EMPRESA, DS_TIPO_LICENCA, NR_LICENCA, DT_EMISSAO, DT_VALIDADE, DS_STATUS, DS_ORGAO_EMISSOR)
VALUES (1, 'Licenca de Operacao', 'LO-2023-001', DATE '2023-01-15', DATE '2026-01-15', 'ATIVA', 'CETESB');

INSERT INTO TB_LICENCA_AMBIENTAL
(ID_EMPRESA, DS_TIPO_LICENCA, NR_LICENCA, DT_EMISSAO, DT_VALIDADE, DS_STATUS, DS_ORGAO_EMISSOR)
VALUES (1, 'Licenca Previa', 'LP-2024-015', DATE '2024-03-10', DATE '2025-06-10', 'ATIVA', 'CETESB');

INSERT INTO TB_LICENCA_AMBIENTAL
(ID_EMPRESA, DS_TIPO_LICENCA, NR_LICENCA, DT_EMISSAO, DT_VALIDADE, DS_STATUS, DS_ORGAO_EMISSOR)
VALUES (2, 'Licenca de Instalacao', 'LI-2024-022', DATE '2024-05-20', DATE '2027-05-20', 'ATIVA', 'IBAMA');

INSERT INTO TB_LICENCA_AMBIENTAL
(ID_EMPRESA, DS_TIPO_LICENCA, NR_LICENCA, DT_EMISSAO, DT_VALIDADE, DS_STATUS, DS_ORGAO_EMISSOR)
VALUES (2, 'Outorga de Agua', 'OUT-2022-088', DATE '2022-08-01', DATE '2024-08-01', 'VENCIDA', 'ANA');

INSERT INTO TB_LICENCA_AMBIENTAL
(ID_EMPRESA, DS_TIPO_LICENCA, NR_LICENCA, DT_EMISSAO, DT_VALIDADE, DS_STATUS, DS_ORGAO_EMISSOR)
VALUES (3, 'Licenca de Operacao', 'LO-2024-103', DATE '2024-11-01', DATE '2026-05-30', 'ATIVA', 'IBAMA');

-- ---------------------------------------------------------
-- AUDITORIAS AMBIENTAIS
-- ---------------------------------------------------------
INSERT INTO TB_AUDITORIA_AMBIENTAL
(ID_EMPRESA, DT_AUDITORIA, DS_RESULTADO, DS_OBSERVACOES, DS_NIVEL_RISCO)
VALUES (1, DATE '2025-02-10', 'Conformidade parcial',
        'Nao conformidades menores na gestao de residuos solidos. Plano de acao definido.', 'MEDIO');

INSERT INTO TB_AUDITORIA_AMBIENTAL
(ID_EMPRESA, DT_AUDITORIA, DS_RESULTADO, DS_OBSERVACOES, DS_NIVEL_RISCO)
VALUES (2, DATE '2025-03-22', 'Conformidade total',
        'Todos os processos em conformidade com a ISO 14001.', 'BAIXO');

INSERT INTO TB_AUDITORIA_AMBIENTAL
(ID_EMPRESA, DT_AUDITORIA, DS_RESULTADO, DS_OBSERVACOES, DS_NIVEL_RISCO)
VALUES (3, DATE '2025-01-05', 'Nao conformidade critica',
        'Vazamento identificado no sistema de armazenamento de combustivel. Acao corretiva imediata exigida.',
        'ALTO');

-- ---------------------------------------------------------
-- EMISSOES DE CARBONO
-- ---------------------------------------------------------
INSERT INTO TB_EMISSAO_CARBONO
(ID_EMPRESA, VL_QUANTIDADE_CO2, DS_FONTE_EMISSAO, DT_REGISTRO, DS_UNIDADE_MEDIDA)
VALUES (1, 1250.5000, 'Caldeiras industriais', DATE '2025-01-31', 'tCO2e');

INSERT INTO TB_EMISSAO_CARBONO
(ID_EMPRESA, VL_QUANTIDADE_CO2, DS_FONTE_EMISSAO, DT_REGISTRO, DS_UNIDADE_MEDIDA)
VALUES (1, 980.7500, 'Frota de veiculos', DATE '2025-02-28', 'tCO2e');

INSERT INTO TB_EMISSAO_CARBONO
(ID_EMPRESA, VL_QUANTIDADE_CO2, DS_FONTE_EMISSAO, DT_REGISTRO, DS_UNIDADE_MEDIDA)
VALUES (2, 320.2500, 'Geracao de energia', DATE '2025-03-15', 'tCO2e');

INSERT INTO TB_EMISSAO_CARBONO
(ID_EMPRESA, VL_QUANTIDADE_CO2, DS_FONTE_EMISSAO, DT_REGISTRO, DS_UNIDADE_MEDIDA)
VALUES (3, 4500.0000, 'Transporte rodoviario', DATE '2025-02-10', 'tCO2e');

-- ---------------------------------------------------------
-- COMPENSACOES AMBIENTAIS
-- ---------------------------------------------------------
INSERT INTO TB_COMPENSACAO_AMBIENTAL
(ID_EMPRESA, DS_TIPO_COMPENSACAO, DS_DESCRICAO, VL_QUANTIDADE_COMPENSADA, DT_ACAO)
VALUES (1, 'Reflorestamento', 'Plantio de 5.000 mudas em area de Mata Atlantica em parceria com a SOS Mata Atlantica',
        1500.0000, DATE '2025-01-20');

INSERT INTO TB_COMPENSACAO_AMBIENTAL
(ID_EMPRESA, DS_TIPO_COMPENSACAO, DS_DESCRICAO, VL_QUANTIDADE_COMPENSADA, DT_ACAO)
VALUES (2, 'Creditos de Carbono', 'Aquisicao de creditos certificados (VCS) de projeto eolico no Ceara',
        500.0000, DATE '2025-04-01');

INSERT INTO TB_COMPENSACAO_AMBIENTAL
(ID_EMPRESA, DS_TIPO_COMPENSACAO, DS_DESCRICAO, VL_QUANTIDADE_COMPENSADA, DT_ACAO)
VALUES (3, 'Energia Renovavel', 'Migracao de 30%% da frota para veiculos eletricos',
        2200.0000, DATE '2025-03-10');

COMMIT;