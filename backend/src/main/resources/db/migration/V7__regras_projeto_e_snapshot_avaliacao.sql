-- Project example plus explicit safety questions. Educational, not clinical validation.
ALTER TABLE avaliacao ALTER COLUMN protocolo_versao TYPE VARCHAR(64);
ALTER TABLE avaliacao ADD COLUMN motivo TEXT;
ALTER TABLE avaliacao ADD COLUMN crianca_nome_snapshot VARCHAR(255);
ALTER TABLE avaliacao ADD COLUMN crianca_idade_snapshot VARCHAR(100);

-- Existing historical results retain their original protocol/version and classification.
UPDATE avaliacao a SET crianca_nome_snapshot = c.nome,
  crianca_idade_snapshot = 'Idade não registrada na avaliação original',
  motivo = 'Avaliação histórica realizada com a versão anterior das regras.'
FROM crianca c WHERE c.id = a.crianca_id;

UPDATE opcao_pergunta SET score = 3, red_flag = FALSE
WHERE pergunta_id IN (SELECT id FROM pergunta WHERE codigo = 'FEBRE_TEMPERATURA') AND codigo = 'ACIMA_39';
UPDATE opcao_pergunta SET texto = 'De 38,5°C até menos de 39°C'
WHERE pergunta_id IN (SELECT id FROM pergunta WHERE codigo = 'FEBRE_TEMPERATURA') AND codigo = 'ENTRE_38_5_39';
UPDATE opcao_pergunta SET texto = 'De 37,8°C até menos de 38,5°C'
WHERE pergunta_id IN (SELECT id FROM pergunta WHERE codigo = 'FEBRE_TEMPERATURA') AND codigo = 'ENTRE_37_8_38_5';
INSERT INTO opcao_pergunta (pergunta_id,codigo,texto,score,red_flag,ordem)
SELECT id,'EXATOS_39','Exatamente 39°C',0,FALSE,4 FROM pergunta WHERE codigo='FEBRE_TEMPERATURA';
UPDATE opcao_pergunta SET ordem=5 WHERE codigo='ACIMA_39' AND pergunta_id IN (SELECT id FROM pergunta WHERE codigo='FEBRE_TEMPERATURA');
INSERT INTO opcao_pergunta (pergunta_id,codigo,texto,score,red_flag,ordem)
SELECT id,'NAO_MEDIDA','Não sei / não consegui medir',3,FALSE,6 FROM pergunta WHERE codigo='FEBRE_TEMPERATURA';

INSERT INTO pergunta (sintoma_id,codigo,texto,sub,tipo,ordem)
SELECT s.id,q.codigo,q.texto,q.sub,'YESNO',q.ordem FROM sintoma s CROSS JOIN (VALUES
('UNIVERSAL_PROSTRACAO','A criança está muito prostrada ou difícil de acordar?','Observe se responde normalmente, interage e permanece acordada.',90),
('UNIVERSAL_RESPIRACAO','Há dificuldade para respirar?','Esforço para respirar, costelas afundando ou lábios arroxeados são sinais de alerta.',91),
('UNIVERSAL_HIDRATACAO','A criança não consegue beber ou mamar, ou quase não urina?','Observe recusa de líquidos, boca muito seca e redução importante da urina.',92),
('UNIVERSAL_MANCHAS','Há manchas roxas que não desaparecem quando pressionadas?','Manchas semelhantes a pequenos hematomas que não clareiam precisam de atenção imediata.',93)
) AS q(codigo,texto,sub,ordem);
INSERT INTO peso_yesno (pergunta_id,score_yes,score_no,score_dunno,red_flag_on_yes,red_flag_on_no)
SELECT id,10,0,3,TRUE,FALSE FROM pergunta WHERE codigo LIKE 'UNIVERSAL_%';
INSERT INTO pergunta (sintoma_id,codigo,texto,sub,tipo,ordem)
SELECT id,'FEBRE_BOM_ESTADO','A criança está em bom estado geral?','Interage normalmente, aceita líquidos e respira confortavelmente.','YESNO',94 FROM sintoma WHERE codigo='FEBRE';
INSERT INTO peso_yesno (pergunta_id,score_yes,score_no,score_dunno,red_flag_on_yes,red_flag_on_no)
SELECT id,0,3,3,FALSE,FALSE FROM pergunta WHERE codigo='FEBRE_BOM_ESTADO';

UPDATE orientacao SET titulo='Avaliação médica em até 24 horas',descricao='Procure pediatra ou pronto atendimento nas próximas horas, em até 24 horas. Se aparecer um sinal de alerta ou houver piora, procure emergência imediatamente.'
WHERE classificacao='MOD' AND tipo IN ('MAIN_ACTION','WHEN_SEEK_HELP');
UPDATE orientacao SET descricao='Este aplicativo educacional apoia a orientação inicial, não fornece diagnóstico e não substitui avaliação médica. As regras do projeto não constituem um protocolo clínico validado.' WHERE tipo='DISCLAIMER';

-- Educational care content for all requested symptoms, without medication dosing.
INSERT INTO orientacao (sintoma_id,classificacao,tipo,titulo,descricao,ordem,ativo)
SELECT s.id,c.classificacao,'HOME_CARE',g.titulo,g.descricao,30,TRUE
FROM sintoma s JOIN (VALUES
('FEBRE','Conforto e hidratação','Ofereça líquidos ou mantenha a amamentação, acompanhe o comportamento e meça a temperatura. Não medique sem orientação adequada à criança.'),
('TOSSE','Observe a respiração','Ofereça líquidos e evite fumaça. Se houver esforço para respirar ou lábios arroxeados, procure emergência imediatamente.'),
('VOMITOS','Líquidos em pequenas quantidades','Ofereça pequenos volumes de líquidos com frequência. Procure ajuda se a criança não conseguir beber ou mamar, ou quase não urinar.'),
('DIARREIA','Acompanhe a hidratação','Mantenha a amamentação e ofereça líquidos. Observe urina e estado geral; sangue nas fezes ou piora precisam de atendimento.'),
('DOR_ABDOMINAL','Acompanhe a dor','Observe localização, intensidade e evolução. Dor forte, barriga endurecida ou piora rápida exigem atendimento imediato.'),
('FALTA_DE_AR','Observe o esforço respiratório','Dificuldade para respirar, costelas afundando ou lábios arroxeados exigem emergência. Não espere completar outra avaliação se esses sinais aparecerem.'),
('MANCHAS_PELE','Observe a pele e o estado geral','Manchas roxas que não clareiam à pressão, inchaço no rosto ou dificuldade para respirar exigem atendimento imediato.'),
('TRAUMA_LEVE','Observe após a queda ou batida','Não force o membro dolorido. Desmaio, confusão, sangramento intenso ou piora precisam de atendimento imediato.'),
('DOR_OUVIDO','Evite manipular o ouvido','Não introduza objetos ou produtos no ouvido sem orientação. Dor persistente, secreção ou inchaço atrás da orelha precisam de avaliação médica.')
) AS g(codigo,titulo,descricao) ON s.codigo=g.codigo
CROSS JOIN (VALUES ('LOW'),('MOD')) AS c(classificacao);
