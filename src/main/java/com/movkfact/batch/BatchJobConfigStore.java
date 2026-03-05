package com.movkfact.batch;

import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store thread-safe pour les configurations de batch jobs.
 * Persiste les configs entre le lancement du job et l'exécution du reader.
 */
@Component
public class BatchJobConfigStore {

    private final ConcurrentHashMap<String, List<BatchDataSetConfigDTO>> configStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> totalCountStore = new ConcurrentHashMap<>();

    /**
     * Stocke les configs d'un batch job sous une clé unique.
     */
    public void storeConfigs(String configKey, List<BatchDataSetConfigDTO> configs) {
        configStore.put(configKey, new ArrayList<>(configs));
    }

    /**
     * Récupère les configs d'un batch job par clé.
     */
    public List<BatchDataSetConfigDTO> retrieveConfigs(String configKey) {
        return configStore.getOrDefault(configKey, Collections.emptyList());
    }

    /**
     * Stocke le nombre total de datasets pour un job (pour calcul de progression).
     */
    public void storeTotalCount(Long jobId, int total) {
        totalCountStore.put(jobId, total);
    }

    /**
     * Récupère le nombre total de datasets pour un job.
     */
    public int getTotalCount(Long jobId) {
        return totalCountStore.getOrDefault(jobId, 0);
    }
}
