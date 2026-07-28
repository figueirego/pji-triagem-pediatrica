-- =========================================================================
-- Ajusta avaliacao para suportar multiplos sintomas por triagem
-- =========================================================================

DROP INDEX IF EXISTS idx_avaliacao_classificacao;

ALTER TABLE avaliacao
    DROP CONSTRAINT IF EXISTS chk_avaliacao_score_min;

ALTER TABLE avaliacao
    ALTER COLUMN respostas DROP NOT NULL;

ALTER TABLE avaliacao
    ADD COLUMN classificacao_final VARCHAR(30),
    ADD COLUMN score_total INT,
    ADD CONSTRAINT chk_avaliacao_score_total_min
        CHECK (score_total >= 0);

CREATE TABLE avaliacao_sintoma (
    id BIGSERIAL PRIMARY KEY,

    avaliacao_id BIGINT NOT NULL
        REFERENCES avaliacao(id) ON DELETE CASCADE,

    sintoma_id BIGINT NOT NULL
        REFERENCES sintoma(id),

    classificacao VARCHAR(30) NOT NULL,

    score INT NOT NULL,

    red_flag_detected BOOLEAN NOT NULL DEFAULT FALSE,

    respostas JSONB NOT NULL,

    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_avaliacao_sintoma_score_min
        CHECK (score >= 0)
);

CREATE INDEX idx_avaliacao_sintoma_avaliacao
    ON avaliacao_sintoma(avaliacao_id);

CREATE INDEX idx_avaliacao_sintoma_sintoma
    ON avaliacao_sintoma(sintoma_id);

INSERT INTO avaliacao_sintoma (
    avaliacao_id,
    sintoma_id,
    classificacao,
    score,
    red_flag_detected,
    respostas,
    criado_em
)
SELECT
    a.id,
    a.sintoma_id,
    COALESCE(a.classificacao, 'LOW'),
    COALESCE(a.score, 0),
    FALSE,
    COALESCE(a.respostas, '{}'::jsonb),
    COALESCE(a.criado_em, NOW())
FROM avaliacao a
WHERE a.sintoma_id IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM information_schema.columns c
      WHERE c.table_schema = current_schema()
        AND c.table_name = 'avaliacao'
        AND c.column_name = 'sintoma_id'
  );

UPDATE avaliacao
SET classificacao_final = COALESCE(classificacao_final, classificacao, 'LOW'),
    score_total = COALESCE(score_total, score, 0);

ALTER TABLE avaliacao
    ALTER COLUMN classificacao_final SET NOT NULL,
    ALTER COLUMN score_total SET NOT NULL;

CREATE INDEX idx_avaliacao_classificacao_final
    ON avaliacao(classificacao_final);

ALTER TABLE avaliacao
    DROP COLUMN IF EXISTS sintoma_id,
    DROP COLUMN IF EXISTS classificacao,
    DROP COLUMN IF EXISTS score;
