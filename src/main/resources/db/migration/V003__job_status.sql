-- V003__job_status.sql
-- Persistent job status for batch jobs (S3.3)
-- Mapped from entity/JobStatus.java
-- Note: job_id is NOT auto-generated — set explicitly by Spring Batch job ID (Long)

CREATE TABLE job_status (
    job_id          BIGINT          PRIMARY KEY,
    status          VARCHAR(50)     NOT NULL,
    progress        INT,
    completed       INT,
    total           INT,
    rows_generated  INT,
    error_count     INT,
    last_error      TEXT,
    created_at      TIMESTAMP,
    completed_at    TIMESTAMP
);
