-- Metadata supports any explicitly configured age/range, without interpreting clinical wording.
ALTER TABLE pergunta
    ADD COLUMN idade_derivada BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN idade_min_valor INTEGER,
    ADD COLUMN idade_min_unidade VARCHAR(10),
    ADD COLUMN idade_min_inclusiva BOOLEAN,
    ADD COLUMN idade_max_valor INTEGER,
    ADD COLUMN idade_max_unidade VARCHAR(10),
    ADD COLUMN idade_max_inclusiva BOOLEAN;

ALTER TABLE pergunta ADD CONSTRAINT chk_pergunta_idade_min CHECK (
    (idade_min_valor IS NULL AND idade_min_unidade IS NULL AND idade_min_inclusiva IS NULL)
    OR (idade_min_valor IS NOT NULL AND idade_min_valor >= 0
        AND idade_min_unidade IS NOT NULL AND idade_min_unidade IN ('DAYS','MONTHS','YEARS')
        AND idade_min_inclusiva IS NOT NULL));
ALTER TABLE pergunta ADD CONSTRAINT chk_pergunta_idade_max CHECK (
    (idade_max_valor IS NULL AND idade_max_unidade IS NULL AND idade_max_inclusiva IS NULL)
    OR (idade_max_valor IS NOT NULL AND idade_max_valor >= 0
        AND idade_max_unidade IS NOT NULL AND idade_max_unidade IN ('DAYS','MONTHS','YEARS')
        AND idade_max_inclusiva IS NOT NULL));
ALTER TABLE pergunta ADD CONSTRAINT chk_pergunta_idade_derivada CHECK (
    (NOT idade_derivada AND idade_min_valor IS NULL AND idade_max_valor IS NULL)
    OR (idade_derivada AND tipo = 'YESNO' AND (idade_min_valor IS NOT NULL OR idade_max_valor IS NOT NULL)));

-- Configure only the condition already supplied by the project; no new medical thresholds.
UPDATE pergunta SET idade_derivada = TRUE,
    idade_max_valor = 3, idade_max_unidade = 'MONTHS', idade_max_inclusiva = FALSE
WHERE codigo = 'FEBRE_BEBE_MENOR_3_MESES';
