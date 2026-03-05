-- V005__column_configs.sql
-- Column configuration for data generation (batch datasets)
-- Mapped from entity/ColumnConfig.java
-- Note: distinct from column_configurations (CSV type detection results, S2.2)

CREATE TABLE column_configs (
    id                BIGSERIAL       PRIMARY KEY,
    name              VARCHAR(255)    NOT NULL,
    column_type       VARCHAR(100)    NOT NULL,
    format            TEXT,
    min_value         INT,
    max_value         INT,
    nullable          BOOLEAN         DEFAULT FALSE,
    additional_config TEXT,
    dataset_id        BIGINT          NOT NULL
);
