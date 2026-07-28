-- =========================================================================
-- Cria tabela de orientacoes da triagem
-- =========================================================================

CREATE TABLE orientacao (
    id BIGSERIAL PRIMARY KEY,
    sintoma_id BIGINT NULL REFERENCES sintoma(id),
    classificacao VARCHAR(30) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao VARCHAR(1500) NOT NULL,
    ordem INT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orientacao_classificacao ON orientacao(classificacao);
CREATE INDEX idx_orientacao_sintoma ON orientacao(sintoma_id);
CREATE INDEX idx_orientacao_tipo ON orientacao(tipo);
CREATE INDEX idx_orientacao_ativo ON orientacao(ativo);

-- Indices unicos para permitir seed idempotente com ON CONFLICT DO NOTHING.
CREATE UNIQUE INDEX uq_orientacao_geral
    ON orientacao (classificacao, tipo, titulo)
    WHERE sintoma_id IS NULL;

CREATE UNIQUE INDEX uq_orientacao_especifica
    ON orientacao (sintoma_id, classificacao, tipo, titulo)
    WHERE sintoma_id IS NOT NULL;
