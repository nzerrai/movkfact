-- V004__activity_tracking.sql
-- Activity tracking for dataset operations (S3.1)
-- Mapped from entity/Activity.java
-- REWRITTEN for PostgreSQL (original used H2/MySQL syntax: AUTO_INCREMENT, LONGTEXT, inline INDEX)
-- Note: No FK to datasets intentionally — activity survives soft-deleted datasets

CREATE TABLE activity (
    id          BIGSERIAL       PRIMARY KEY,
    dataset_id  BIGINT          NOT NULL,
    action      VARCHAR(50)     NOT NULL,
    timestamp   TIMESTAMP       DEFAULT NOW(),
    user_name   VARCHAR(255),
    created_at  TIMESTAMP       DEFAULT NOW()
);

CREATE INDEX idx_activity_dataset   ON activity (dataset_id);
CREATE INDEX idx_activity_timestamp ON activity (timestamp);
