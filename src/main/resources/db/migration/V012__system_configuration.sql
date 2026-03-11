-- V012__system_configuration.sql
-- Table pour gérer les paramètres de configuration système

CREATE TABLE IF NOT EXISTS system_configuration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    value_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
);

-- Insert default configurations
INSERT INTO system_configuration (config_key, config_value, description, value_type) 
VALUES 
('max_columns_per_dataset', '50', 'Maximum number of columns allowed per dataset (including extra columns)', 'INTEGER'),
('max_csv_file_size_mb', '10', 'Maximum CSV file size in megabytes', 'INTEGER'),
('max_rows_per_generation', '100000', 'Maximum number of rows to generate in one request', 'INTEGER')
ON DUPLICATE KEY UPDATE config_key = config_key;