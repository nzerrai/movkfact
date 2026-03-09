package com.movkfact.service;

import com.movkfact.entity.ColumnConfig;
import com.movkfact.entity.ColumnConfiguration;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.ColumnConfigRepository;
import com.movkfact.repository.ColumnConfigurationRepository;
import com.movkfact.repository.DataSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing column configurations.
 *
 * <p>Primary source: {@code column_configurations} table (populated on CSV upload or manual
 * generation). The {@code additionalConfig} JSON field carries type-specific constraints
 * (e.g., ENUM values).</p>
 *
 * <p>Fallback: {@code column_configs} table of the most recent dataset of the domain,
 * used for domains whose schema was never saved to {@code column_configurations}.</p>
 */
@Service
public class ColumnConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnConfigurationService.class);

    @Autowired
    private ColumnConfigurationRepository columnConfigurationRepository;

    @Autowired
    private ColumnConfigRepository columnConfigRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    /**
     * Save column configurations for a domain (replaces existing ones).
     * Accepts an optional {@code "additionalConfig"} key in each column map
     * to persist type-specific constraints (ENUM values, etc.).
     */
    @Transactional
    public List<ColumnConfiguration> saveColumnConfigurations(Long domainId, List<Map<String, Object>> columns) {
        logger.info("Saving {} column configurations for domain {}", columns.size(), domainId);

        columnConfigurationRepository.deleteByDomainId(domainId);

        List<ColumnConfiguration> configurations = columns.stream()
                .map(col -> {
                    ColumnConfiguration cc = new ColumnConfiguration(
                            domainId,
                            (String) col.get("name"),
                            (String) col.get("type"),
                            ((Number) col.get("confidence")).doubleValue(),
                            (String) col.get("detector")
                    );
                    Object ac = col.get("additionalConfig");
                    if (ac instanceof String s) {
                        cc.setAdditionalConfig(s);
                    }
                    return cc;
                })
                .collect(Collectors.toList());

        List<ColumnConfiguration> saved = columnConfigurationRepository.saveAll(configurations);
        logger.info("Saved {} column configurations for domain {}", saved.size(), domainId);
        return saved;
    }

    /**
     * Get all column configurations for a domain from the primary table.
     */
    public List<ColumnConfiguration> getColumnConfigurations(Long domainId) {
        logger.debug("Retrieving column configurations for domain {}", domainId);
        return columnConfigurationRepository.findByDomainId(domainId);
    }

    /**
     * Get column configurations as formatted list of maps, including {@code additionalConfig}.
     *
     * <p>If the primary {@code column_configurations} table has no entries for this domain,
     * falls back to the {@code column_configs} of the most recently created dataset.</p>
     */
    public List<Map<String, ? extends Object>> getColumnConfigurationsAsMap(Long domainId) {
        List<ColumnConfiguration> primary = getColumnConfigurations(domainId);

        if (!primary.isEmpty()) {
            return primary.stream()
                    .map(config -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", config.getId());
                        map.put("name", config.getColumnName());
                        map.put("type", config.getDetectedType());
                        map.put("confidence", config.getConfidence());
                        map.put("detector", config.getDetector());
                        map.put("additionalConfig", config.getAdditionalConfig());
                        return map;
                    })
                    .collect(Collectors.toList());
        }

        // Fallback: domain created via wizard — use latest dataset's column_configs
        Optional<DataSet> latest = dataSetRepository
                .findTopByDomainIdAndDeletedAtIsNullOrderByCreatedAtDesc(domainId);

        if (latest.isEmpty()) {
            logger.debug("No column configurations and no dataset found for domain {}", domainId);
            return List.of();
        }

        List<ColumnConfig> colConfigs = columnConfigRepository.findByDatasetId(latest.get().getId());
        logger.debug("Fallback: using {} column_configs from dataset {} for domain {}",
                colConfigs.size(), latest.get().getId(), domainId);

        return colConfigs.stream()
                .map(col -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", col.getId());
                    map.put("name", col.getName());
                    map.put("type", col.getColumnType().name());
                    map.put("confidence", 1.0);
                    map.put("detector", "manual");
                    map.put("additionalConfig", col.getAdditionalConfig());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if domain has any usable column configurations (primary or fallback).
     */
    public boolean hasConfigurations(Long domainId) {
        if (columnConfigurationRepository.existsByDomainId(domainId)) {
            return true;
        }
        return dataSetRepository
                .findTopByDomainIdAndDeletedAtIsNullOrderByCreatedAtDesc(domainId)
                .map(ds -> !columnConfigRepository.findByDatasetId(ds.getId()).isEmpty())
                .orElse(false);
    }

    /**
     * Delete column configurations for a domain.
     */
    @Transactional
    public void deleteColumnConfigurations(Long domainId) {
        logger.info("Deleting column configurations for domain {}", domainId);
        columnConfigurationRepository.deleteByDomainId(domainId);
    }
}
