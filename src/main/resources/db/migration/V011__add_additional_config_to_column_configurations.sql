-- V011: Ajoute additional_config pour stocker les contraintes JSON
-- (valeurs ENUM, plages numériques, etc.) dans column_configurations.
ALTER TABLE column_configurations ADD COLUMN IF NOT EXISTS additional_config TEXT;
