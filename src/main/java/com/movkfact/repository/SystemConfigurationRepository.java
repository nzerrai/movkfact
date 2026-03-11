package com.movkfact.repository;

import com.movkfact.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des paramètres de configuration système.
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    
    /**
     * Récupère un paramètre de configuration par sa clé.
     */
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    
    /**
     * Vérifie l'existence d'un paramètre par sa clé.
     */
    boolean existsByConfigKey(String configKey);
}