-- V001__initial_schema.sql
-- Initial schema: domain_master table
-- Mapped from entity/Domain.java

CREATE TABLE domain_master (
    id          BIGSERIAL       PRIMARY KEY,
    version     BIGINT          NOT NULL DEFAULT 0,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(2000),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    deleted_at  TIMESTAMP,
    CONSTRAINT uk_domain_name UNIQUE (name)
);

CREATE INDEX idx_domain_name       ON domain_master (name);
CREATE INDEX idx_domain_deleted_at ON domain_master (deleted_at);
