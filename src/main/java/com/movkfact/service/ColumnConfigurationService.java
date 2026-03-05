package com.movkfact.service;

import com.movkfact.entity.ColumnConfiguration;
import com.movkfact.repository.ColumnConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing column configurations
 */
@Service
public class ColumnConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnConfigurationService.class);

    @Autowired
    private ColumnConfigurationRepository columnConfigurationRepository;

    /**
     * Save column configurations for a domain
     * Replaces existing configurations if any
     *
     * @param domainId the domain ID
     * @param columns the column data (list of maps with name, type, confidence, detector)
     * @return list of saved configurations
     */
    @Transactional
    public List<ColumnConfiguration> saveColumnConfigurations(Long domainId, List<Map<String, Object>> columns) {
        logger.info("Saving {} column configurations for domain {}", columns.size(), domainId);

        // Delete existing configurations
        columnConfigurationRepository.deleteByDomainId(domainId);

        // Create and save new configurations
        List<ColumnConfiguration> configurations = columns.stream()
                .map(col -> new ColumnConfiguration(
                        domainId,
                        (String) col.get("name"),
                        (String) col.get("type"),
                        ((Number) col.get("confidence")).doubleValue(),
                        (String) col.get("detector")
                ))
                .collect(Collectors.toList());

        List<ColumnConfiguration> saved = columnConfigurationRepository.saveAll(configurations);
        logger.info("Saved {} column configurations for domain {}", saved.size(), domainId);

        return saved;
    }

    /**
     * Get all column configurations for a domain
     *
     * @param domainId the domain ID
     * @return list of configurations
     */
    public List<ColumnConfiguration> getColumnConfigurations(Long domainId) {
        logger.debug("Retrieving column configurations for domain {}", domainId);
        return columnConfigurationRepository.findByDomainId(domainId);
    }

    /**
     * Get column configurations as formatted list of maps
     *
     * @param domainId the domain ID
     * @return list of configuration maps
     */
    public List<Map<String, ? extends Object>> getColumnConfigurationsAsMap(Long domainId) {
        return getColumnConfigurations(domainId).stream()
                .map(config -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", config.getId());
                    map.put("name", config.getColumnName());
                    map.put("type", config.getDetectedType());
                    map.put("confidence", config.getConfidence());
                    map.put("detector", config.getDetector());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if domain has any configurations
     *
     * @param domainId the domain ID
     * @return true if configurations exist
     */
    public boolean hasConfigurations(Long domainId) {
        return columnConfigurationRepository.existsByDomainId(domainId);
    }

    /**
     * Delete column configurations for a domain
     *
     * @param domainId the domain ID
     */
    @Transactional
    public void deleteColumnConfigurations(Long domainId) {
        logger.info("Deleting column configurations for domain {}", domainId);
        columnConfigurationRepository.deleteByDomainId(domainId);
    }
}
