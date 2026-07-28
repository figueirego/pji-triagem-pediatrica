-- =========================================================================
-- Seed inicial de orientacoes gerais e especificas por sintoma
-- =========================================================================

-- -------------------------------------------------------------------------
-- Orientacoes gerais LOW
-- -------------------------------------------------------------------------
INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
VALUES
    (NULL, 'LOW', 'SUMMARY', 'Baixo risco', 'Os sinais informados sugerem baixo risco no momento.', 1, TRUE),
    (NULL, 'LOW', 'MAIN_ACTION', 'Observe a evolucao', 'Observe a evolucao dos sintomas em casa.', 2, TRUE),
    (NULL, 'LOW', 'HOME_CARE', 'Cuidados em casa', 'Ofereca liquidos e mantenha a crianca em ambiente confortavel.', 3, TRUE),
    (NULL, 'LOW', 'WHEN_SEEK_HELP', 'Quando procurar ajuda', 'Procure atendimento se os sintomas piorarem ou persistirem.', 4, TRUE),
    (NULL, 'LOW', 'DISCLAIMER', 'Aviso importante', 'Este aplicativo nao substitui avaliacao medica profissional.', 99, TRUE)
ON CONFLICT DO NOTHING;

-- -------------------------------------------------------------------------
-- Orientacoes gerais MOD
-- -------------------------------------------------------------------------
INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
VALUES
    (NULL, 'MOD', 'SUMMARY', 'Risco moderado', 'Os sinais informados sugerem que a crianca precisa de acompanhamento mais proximo.', 1, TRUE),
    (NULL, 'MOD', 'MAIN_ACTION', 'Acompanhe de perto', 'Acompanhe de perto e considere procurar atendimento.', 2, TRUE),
    (NULL, 'MOD', 'WARNING_SIGN', 'Sinais de alerta', 'Procure atendimento se houver piora, sonolencia intensa ou dificuldade para respirar.', 3, TRUE),
    (NULL, 'MOD', 'HOME_CARE', 'Cuidados em casa', 'Mantenha hidratacao e observe temperatura, respiracao e comportamento.', 4, TRUE),
    (NULL, 'MOD', 'WHEN_SEEK_HELP', 'Quando procurar ajuda', 'Procure uma unidade de saude se os sintomas persistirem ou aumentarem.', 5, TRUE),
    (NULL, 'MOD', 'DISCLAIMER', 'Aviso importante', 'Este aplicativo nao substitui avaliacao medica profissional.', 99, TRUE)
ON CONFLICT DO NOTHING;

-- -------------------------------------------------------------------------
-- Orientacoes gerais HIGH
-- -------------------------------------------------------------------------
INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
VALUES
    (NULL, 'HIGH', 'SUMMARY', 'Alto risco', 'A triagem identificou sinais que exigem avaliacao medica imediata.', 1, TRUE),
    (NULL, 'HIGH', 'MAIN_ACTION', 'Procure atendimento imediatamente', 'Procure atendimento imediatamente.', 2, TRUE),
    (NULL, 'HIGH', 'WARNING_SIGN', 'Sinais de alerta', 'Sinais de alerta foram identificados na triagem.', 3, TRUE),
    (NULL, 'HIGH', 'HOME_CARE', 'Cuidados enquanto busca atendimento', 'Nao deixe a crianca sozinha e observe respiracao, consciencia e hidratacao.', 4, TRUE),
    (NULL, 'HIGH', 'WHEN_SEEK_HELP', 'Quando procurar ajuda', 'Procure uma unidade de urgencia ou emergencia.', 5, TRUE),
    (NULL, 'HIGH', 'DISCLAIMER', 'Aviso importante', 'Este aplicativo nao substitui avaliacao medica profissional.', 99, TRUE)
ON CONFLICT DO NOTHING;

-- -------------------------------------------------------------------------
-- Orientacoes especificas por sintoma existente no seed
-- -------------------------------------------------------------------------
INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'HIGH', 'WARNING_SIGN', 'Febre alta ou persistente', 'Febre alta, persistente ou em bebe pequeno exige atencao imediata.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'FEBRE'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'MOD', 'HOME_CARE', 'Acompanhe a tosse', 'Observe se a tosse piora ou aparece chiado no peito.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'TOSSE'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'MOD', 'HOME_CARE', 'Hidratacao apos vomitos', 'Ofereca pequenos volumes de liquidos com frequencia e observe sinais de desidratacao.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'VOMITOS'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'MOD', 'HOME_CARE', 'Hidratacao na diarreia', 'Ofereca liquidos e observe a frequencia das evacuacoes e a aceitacao de liquidos.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'DIARREIA'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'HIGH', 'WARNING_SIGN', 'Risco de desidratacao', 'Diarreia intensa, sangue nas fezes ou sinais de desidratacao exigem avaliacao rapida.', 21, TRUE
FROM sintoma s
WHERE s.codigo = 'DIARREIA'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'HIGH', 'WARNING_SIGN', 'Dificuldade para respirar', 'Dificuldade para respirar e sinal de alerta.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'FALTA_DE_AR'
ON CONFLICT DO NOTHING;

INSERT INTO orientacao (sintoma_id, classificacao, tipo, titulo, descricao, ordem, ativo)
SELECT s.id, 'HIGH', 'WARNING_SIGN', 'Dor abdominal intensa', 'Dor abdominal forte, barriga dura ou piora rapida exigem avaliacao imediata.', 20, TRUE
FROM sintoma s
WHERE s.codigo = 'DOR_ABDOMINAL'
ON CONFLICT DO NOTHING;
