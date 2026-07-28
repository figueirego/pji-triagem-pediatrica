-- =========================================================================
-- Seed inicial de sintomas e questionários base para testes de triagem
-- =========================================================================

-- -------------------------------------------------------------------------
-- Sintomas exibidos no protótipo
-- -------------------------------------------------------------------------
INSERT INTO sintoma (codigo, nome, descricao_curta, icone_ref, cor_hex, ordem)
VALUES
    ('FEBRE', 'Febre', 'Temperatura acima de 37.8°C', 'thermo', '#F59E5C', 1),
    ('TOSSE', 'Tosse', 'Seca, com catarro ou persistente', 'cough', '#56CCF2', 2),
    ('VOMITOS', 'Vômitos', 'Náuseas ou episódios de vômito', 'vomit', '#9B7BE0', 3),
    ('DIARREIA', 'Diarreia', 'Fezes líquidas ou frequentes', 'drop', '#56CCF2', 4),
    ('DOR_ABDOMINAL', 'Dor abdominal', 'Dor ou desconforto na barriga', 'belly', '#F2C94C', 5),
    ('FALTA_DE_AR', 'Falta de ar', 'Respiração rápida ou difícil', 'lung', '#EB5757', 6),
    ('MANCHAS_PELE', 'Manchas na pele', 'Vermelhidão, pintas ou erupção', 'rash', '#E18ABF', 7),
    ('TRAUMA_LEVE', 'Trauma leve', 'Quedas, batidas ou cortes pequenos', 'bandage', '#7BC393', 8),
    ('DOR_OUVIDO', 'Dor de ouvido', 'Dor, coceira ou secreção', 'ear', '#56CCF2', 9)
ON CONFLICT (codigo) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas FEBRE
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'FEBRE_TEMPERATURA', 'Qual a temperatura medida?', 'Escolha a faixa mais próxima.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'FEBRE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'FEBRE_MAIS_3_DIAS', 'A febre dura há mais de 3 dias?', 'Considere desde o primeiro dia de febre.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'FEBRE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'FEBRE_BEBE_MENOR_3_MESES', 'A criança tem menos de 3 meses e está com febre?', 'Febre em bebês pequenos exige atenção.', 'YESNO', 3
FROM sintoma s
WHERE s.codigo = 'FEBRE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MENOR_37_8', 'Menor que 37,8°C', 0, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_TEMPERATURA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'ENTRE_37_8_38_5', 'Entre 37,8°C e 38,5°C', 3, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_TEMPERATURA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'ENTRE_38_5_39', 'Entre 38,5°C e 39°C', 6, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_TEMPERATURA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'ACIMA_39', 'Acima de 39°C', 9, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_TEMPERATURA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 7, 0, 3, FALSE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_MAIS_3_DIAS'
ON CONFLICT (pergunta_id) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FEBRE'
  AND p.codigo = 'FEBRE_BEBE_MENOR_3_MESES'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas TOSSE
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'TOSSE_INTENSIDADE', 'Como está a tosse?', 'Escolha a opção que mais se aproxima.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'TOSSE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'TOSSE_COM_FEBRE', 'A tosse está acompanhada de febre?', 'Febre junto com tosse pode indicar maior atenção.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'TOSSE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'TOSSE_CHIADO', 'A criança apresenta chiado no peito?', 'Perceba se há ruído ao respirar.', 'YESNO', 3
FROM sintoma s
WHERE s.codigo = 'TOSSE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'LEVE', 'Tosse leve e ocasional', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'FREQUENTE', 'Tosse frequente', 5, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MUITO_FORTE', 'Tosse muito forte ou crises', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'COM_ENGASGO', 'Tosse com engasgos ou lábios arroxeados', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 5, 0, 2, FALSE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_COM_FEBRE'
ON CONFLICT (pergunta_id) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 8, 0, 4, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TOSSE'
  AND p.codigo = 'TOSSE_CHIADO'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas VÔMITOS
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'VOMITOS_FREQUENCIA', 'Quantas vezes a criança vomitou nas últimas horas?', 'Considere as últimas 6 horas.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'VOMITOS'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'VOMITOS_DESIDRATACAO', 'Há sinais de desidratação?', 'Boca seca, pouca urina, sonolência ou olhos fundos.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'VOMITOS'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'UMA_VEZ', 'Uma vez', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'VOMITOS'
  AND p.codigo = 'VOMITOS_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'DUAS_TRES', 'Duas a três vezes', 5, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'VOMITOS'
  AND p.codigo = 'VOMITOS_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'VARIAS', 'Várias vezes', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'VOMITOS'
  AND p.codigo = 'VOMITOS_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'NAO_PARA', 'Vomita tudo que ingere', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'VOMITOS'
  AND p.codigo = 'VOMITOS_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'VOMITOS'
  AND p.codigo = 'VOMITOS_DESIDRATACAO'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas DIARREIA
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DIARREIA_FREQUENCIA', 'Quantas evacuações líquidas ocorreram hoje?', 'Considere desde que acordou.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'DIARREIA'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DIARREIA_SANGUE', 'Há sangue nas fezes?', 'Marque sim se viu sangue ou fezes muito escuras.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'DIARREIA'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'UMA_DUAS', 'Uma a duas vezes', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DIARREIA'
  AND p.codigo = 'DIARREIA_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'TRES_CINCO', 'Três a cinco vezes', 5, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DIARREIA'
  AND p.codigo = 'DIARREIA_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MAIS_CINCO', 'Mais de cinco vezes', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DIARREIA'
  AND p.codigo = 'DIARREIA_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MUITO_FREQUENTE', 'Muito frequente ou sem conseguir hidratar', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DIARREIA'
  AND p.codigo = 'DIARREIA_FREQUENCIA'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DIARREIA'
  AND p.codigo = 'DIARREIA_SANGUE'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas DOR ABDOMINAL
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DOR_ABD_INTENSIDADE', 'Como está a dor abdominal?', 'Escolha conforme o comportamento da criança.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'DOR_ABDOMINAL'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DOR_ABD_BARRIGA_DURA', 'A barriga está dura, inchada ou a dor está concentrada de um lado?', 'Esses sinais podem indicar maior urgência.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'DOR_ABDOMINAL'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'LEVE', 'Leve, a criança segue brincando', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_ABDOMINAL'
  AND p.codigo = 'DOR_ABD_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MODERADA', 'Moderada, incomoda bastante', 5, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_ABDOMINAL'
  AND p.codigo = 'DOR_ABD_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'FORTE', 'Forte, chora ou evita se mexer', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_ABDOMINAL'
  AND p.codigo = 'DOR_ABD_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MUITO_FORTE', 'Muito forte ou piora rápida', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_ABDOMINAL'
  AND p.codigo = 'DOR_ABD_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_ABDOMINAL'
  AND p.codigo = 'DOR_ABD_BARRIGA_DURA'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas FALTA DE AR
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'FALTA_AR_INTENSIDADE', 'Como está a respiração?', 'Observe esforço, cansaço ou dificuldade para falar ou chorar.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'FALTA_DE_AR'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'FALTA_AR_LABIOS_ROXOS', 'Os lábios ou dedos estão arroxeados?', 'Alteração de cor pode indicar urgência.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'FALTA_DE_AR'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'NORMAL', 'Respiração normal', 0, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FALTA_DE_AR'
  AND p.codigo = 'FALTA_AR_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'RAPIDA', 'Respiração rápida', 6, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FALTA_DE_AR'
  AND p.codigo = 'FALTA_AR_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'COM_ESFORCO', 'Respiração com esforço', 9, TRUE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FALTA_DE_AR'
  AND p.codigo = 'FALTA_AR_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'MUITO_DIFICIL', 'Muita dificuldade para respirar', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FALTA_DE_AR'
  AND p.codigo = 'FALTA_AR_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'FALTA_DE_AR'
  AND p.codigo = 'FALTA_AR_LABIOS_ROXOS'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas MANCHAS NA PELE
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'MANCHAS_PELE_ASPECTO', 'Como estão as manchas na pele?', 'Observe quantidade, extensão e aparência geral.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'MANCHAS_PELE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'MANCHAS_PELE_NAO_DESAPARECEM', 'As manchas não desaparecem ao pressionar?', 'Esse é um sinal que merece atenção.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'MANCHAS_PELE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'POUCAS', 'Poucas manchas localizadas', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'MANCHAS_PELE'
  AND p.codigo = 'MANCHAS_PELE_ASPECTO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'AREA_LIMITADA', 'Manchas em uma área limitada', 4, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'MANCHAS_PELE'
  AND p.codigo = 'MANCHAS_PELE_ASPECTO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'ESPALHADAS', 'Manchas espalhadas em várias regiões', 7, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'MANCHAS_PELE'
  AND p.codigo = 'MANCHAS_PELE_ASPECTO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'COM_INCHACO', 'Manchas com inchaço facial ou falta de ar', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'MANCHAS_PELE'
  AND p.codigo = 'MANCHAS_PELE_ASPECTO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'MANCHAS_PELE'
  AND p.codigo = 'MANCHAS_PELE_NAO_DESAPARECEM'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas TRAUMA LEVE
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'TRAUMA_LEVE_LESAO', 'Como está a lesão ou dor após o trauma?', 'Considere dor, inchaço, corte ou dificuldade para apoiar.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'TRAUMA_LEVE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'TRAUMA_LEVE_CONFUSAO', 'Houve desmaio, confusão ou vômitos repetidos após o trauma?', 'Sinais neurológicos aumentam a urgência.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'TRAUMA_LEVE'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'LEVE', 'Dor leve ou arranhão superficial', 1, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TRAUMA_LEVE'
  AND p.codigo = 'TRAUMA_LEVE_LESAO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'INCOMODA', 'Dor local ou hematoma pequeno', 4, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TRAUMA_LEVE'
  AND p.codigo = 'TRAUMA_LEVE_LESAO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'DIFICULTA', 'Dor forte ou evita apoiar ou mexer', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TRAUMA_LEVE'
  AND p.codigo = 'TRAUMA_LEVE_LESAO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'DEFORMIDADE', 'Deformidade, sangramento intenso ou desmaio', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TRAUMA_LEVE'
  AND p.codigo = 'TRAUMA_LEVE_LESAO'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 10, 0, 5, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'TRAUMA_LEVE'
  AND p.codigo = 'TRAUMA_LEVE_CONFUSAO'
ON CONFLICT (pergunta_id) DO NOTHING;

-- -------------------------------------------------------------------------
-- Perguntas DOR DE OUVIDO
-- -------------------------------------------------------------------------
INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DOR_OUVIDO_INTENSIDADE', 'Como está a dor de ouvido?', 'Observe o incômodo, o choro e a evolução.', 'OPTIONS', 1
FROM sintoma s
WHERE s.codigo = 'DOR_OUVIDO'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO pergunta (sintoma_id, codigo, texto, sub, tipo, ordem)
SELECT s.id, 'DOR_OUVIDO_SECRECAO', 'Há secreção, sangue ou febre alta junto da dor?', 'Esses sinais podem indicar maior gravidade.', 'YESNO', 2
FROM sintoma s
WHERE s.codigo = 'DOR_OUVIDO'
ON CONFLICT (sintoma_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'LEVE', 'Desconforto leve ou esporádico', 2, FALSE, 1
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_OUVIDO'
  AND p.codigo = 'DOR_OUVIDO_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'PERSISTENTE', 'Dor persistente', 5, FALSE, 2
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_OUVIDO'
  AND p.codigo = 'DOR_OUVIDO_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'FORTE', 'Dor forte ou choro intenso', 8, FALSE, 3
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_OUVIDO'
  AND p.codigo = 'DOR_OUVIDO_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_id, codigo, texto, score, red_flag, ordem)
SELECT p.id, 'COM_INCHACO', 'Dor com inchaço atrás da orelha', 10, TRUE, 4
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_OUVIDO'
  AND p.codigo = 'DOR_OUVIDO_INTENSIDADE'
ON CONFLICT (pergunta_id, codigo) DO NOTHING;

INSERT INTO peso_yesno (pergunta_id, score_yes, score_no, score_dunno, red_flag_on_yes, red_flag_on_no)
SELECT p.id, 9, 0, 4, TRUE, FALSE
FROM pergunta p
JOIN sintoma s ON s.id = p.sintoma_id
WHERE s.codigo = 'DOR_OUVIDO'
  AND p.codigo = 'DOR_OUVIDO_SECRECAO'
ON CONFLICT (pergunta_id) DO NOTHING;
