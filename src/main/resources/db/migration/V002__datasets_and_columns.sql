-- V002__datasets_and_columns.sql
-- datasets and column_configurations tables
-- Mapped from entity/DataSet.java and entity/ColumnConfiguration.java
-- Note: LONGTEXT (H2/MySQL) replaced by TEXT (PostgreSQL)

CREATE TABLE datasets (
    id                  BIGSERIAL       PRIMARY KEY,
    domain_id           BIGINT          NOT NULL,
    dataset_name        VARCHAR(255)    NOT NULL,
    row_count           INT             NOT NULL,
    column_count        INT,
    generation_time_ms  BIGINT          NOT NULL,
    data_json           TEXT,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    deleted_at          TIMESTAMP,
    version             INT             NOT NULL DEFAULT 0,
    original_data       TEXT,
    CONSTRAINT uk_domain_dataset_name UNIQUE (domain_id, dataset_name)
);

CREATE INDEX idx_domain_id         ON datasets (domain_id);
CREATE INDEX idx_deleted_at        ON datasets (deleted_at);
CREATE INDEX idx_domain_dataset_nm ON datasets (domain_id, dataset_name);

-- Column configurations: CSV column type detection results (S2.2)
CREATE TABLE column_configurations (
    id              BIGSERIAL       PRIMARY KEY,
    domain_id       BIGINT          NOT NULL,
    column_name     VARCHAR(255)    NOT NULL,
    detected_type   VARCHAR(255)    NOT NULL,
    confidence      FLOAT8          NOT NULL,
    detector        TEXT,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);
