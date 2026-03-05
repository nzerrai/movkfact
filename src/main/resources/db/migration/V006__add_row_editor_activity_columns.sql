-- V006__add_row_editor_activity_columns.sql
-- Adds row-level tracking columns to activity table (S6.1)
-- PostgreSQL syntax — colonnes NULLABLE pour zéro régression sur Activity existants
-- Voir V004__activity_tracking.sql pour référence syntaxe

ALTER TABLE activity ADD COLUMN row_index       INTEGER;
ALTER TABLE activity ADD COLUMN modified_columns TEXT;
ALTER TABLE activity ADD COLUMN previous_value   TEXT;
