package com.movkfact.service;

import com.movkfact.entity.SystemConfiguration;
import com.movkfact.repository.SystemConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les paramètres de configuration système.
 * Permet de récupérer et mettre à jour les configurations stockées en base de données.
 */
@Service
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private SystemConfigurationRepository systemConfigurationRepository;
    
    /**
     * Récupère un paramètre de configuration par sa clé.
     */
    public Optional<SystemConfiguration> getConfiguration(String configKey) {
        logger.debug("Retrieving configuration: {}", configKey);
        return systemConfigurationRepository.findByConfigKey(configKey);
    }
    
    /**
     * Récupère TOUS les paramètres de configuration.
     */
    public List<SystemConfiguration> getAllConfigurations() {
        logger.debug("Retrieving all configurations");
        return systemConfigurationRepository.findAll();
    }
    
    /**
     * Récupère la valeur d'une configuration en tant que String.
     * Mis en cache pour performance optimale.
     */
    @Cacheable(value = "configCache", key = "#configKey")
    public String getConfigurationValue(String configKey, String defaultValue) {
        return systemConfigurationRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(defaultValue);
    }
    
    /**
     * Récupère la valeur d'une configuration en tant que Integer.
     * Mis en cache pour performance optimale.
     */
    @Cacheable(value = "configCache", key = "#configKey")
    public Integer getConfigurationAsInteger(String configKey, Integer defaultValue) {
        try {
            String value = systemConfigurationRepository.findByConfigKey(configKey)
                    .map(SystemConfiguration::getConfigValue)
                    .orElse(null);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            logger.warn("Configuration {} is not a valid integer, using default: {}", configKey, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Récupère la valeur d'une configuration en tant que Boolean.
     * Mis en cache pour performance optimale.
     */
    @Cacheable(value = "configCache", key = "#configKey")
    public Boolean getConfigurationAsBoolean(String configKey, Boolean defaultValue) {
        String value = systemConfigurationRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    /**
     * Crée ou met à jour une configuration.
     * Valide les types de valeur selon le valueType spécifié.
     */
    @Transactional
    public SystemConfiguration saveConfiguration(String configKey, String configValue, String description, String valueType) {
        logger.info("Saving configuration: {} = {} (type: {})", configKey, configValue, valueType);
        
        // Validate value based on type
        validateConfigurationValue(configValue, valueType);
        
        Optional<SystemConfiguration> existing = systemConfigurationRepository.findByConfigKey(configKey);
        
        SystemConfiguration config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setConfigValue(configValue);
            config.setDescription(description);
            config.setValueType(valueType);
        } else {
            config = new SystemConfiguration(configKey, configValue, description, valueType);
        }
        
        return systemConfigurationRepository.save(config);
    }
    
    /**
     * Supprime une configuration.
     */
    @Transactional
    public void deleteConfiguration(String configKey) {
        logger.info("Deleting configuration: {}", configKey);
        systemConfigurationRepository.findByConfigKey(configKey)
                .ifPresent(config -> systemConfigurationRepository.deleteById(config.getId()));
    }
    
    /**
     * Valide qu'une valeur de configuration est compatible avec son type.
     */
    private void validateConfigurationValue(String value, String valueType) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Configuration value cannot be null or empty");
        }
        
        switch (valueType.toUpperCase()) {
            case "INTEGER":
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Configuration value must be a valid integer for type INTEGER: " + value);
                }
                break;
            case "BOOLEAN":
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Configuration value must be 'true' or 'false' for type BOOLEAN: " + value);
                }
                break;
            case "STRING":
                // Any non-empty string is valid
                break;
            default:
                logger.warn("Unknown configuration type: {}", valueType);
        }
    }
}