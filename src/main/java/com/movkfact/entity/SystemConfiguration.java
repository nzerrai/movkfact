package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité pour les paramètres de configuration système.
 * Permet de gérer les limites et configurations de l'application via la base de données.
 */
@Entity
@Table(name = "system_configuration")
public class SystemConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "value_type", length = 50, nullable = false)
    private String valueType; // STRING, INTEGER, BOOLEAN, DECIMAL
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SystemConfiguration() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public SystemConfiguration(String configKey, String configValue, String description, String valueType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.valueType = valueType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getValueType() {
        return valueType;
    }
    
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}