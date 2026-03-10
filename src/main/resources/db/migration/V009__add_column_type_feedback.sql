-- V009__add_column_type_feedback.sql
-- Table de corpus d'apprentissage adaptatif (S10.1).
-- Stocke les mappings validés par l'utilisateur : nom de colonne normalisé → type validé.
-- Le count s'incrémente à chaque nouvelle validation du même mapping.
-- domain_id NULL = mapping global (cross-domain) ; non NULL = spécifique à un domaine.

CREATE TABLE column_type_feedback (
    id                     BIGSERIAL    PRIMARY KEY,
    column_name_normalized VARCHAR(100) NOT NULL,
    validated_type         VARCHAR(50)  NOT NULL,
    domain_id              BIGINT       REFERENCES domain_master(id) ON DELETE SET NULL,
    count                  INT          NOT NULL DEFAULT 1,
    last_seen              TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Contrainte d'unicité fonctionnelle (COALESCE pour gérer domain_id NULL, car NULL != NULL en SQL)
CREATE UNIQUE INDEX uq_feedback ON column_type_feedback
    (column_name_normalized, validated_type, COALESCE(domain_id, -1));

CREATE INDEX idx_feedback_lookup ON column_type_feedback (column_name_normalized, domain_id);
