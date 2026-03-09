-- V008__add_activity_action_index.sql
-- Adds composite index on activity(dataset_id, action) for DomainService status queries (S8.1).
-- V004 already creates idx_activity_dataset on dataset_id alone.
-- This composite index avoids full scans when filtering by both dataset_id and action type.

CREATE INDEX IF NOT EXISTS idx_activity_dataset_action ON activity (dataset_id, action);
