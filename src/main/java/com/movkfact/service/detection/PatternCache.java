package com.movkfact.service.detection;

import com.movkfact.entity.DetectionPattern;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.DetectionPatternRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Singleton component that compiles and caches regex patterns at startup.
 *
 * <p>Les patterns sont chargés depuis la table {@code detection_pattern} en BDD (S10.2).
 * {@link #reload()} peut être appelé à chaud après toute modification en BDD.</p>
 *
 * <p>Thread-safety : {@link #reload()} est {@code synchronized} pour éviter les races
 * lors de recharges concurrentes via l'API REST.</p>
 */
@Component
public class PatternCache {

    private static final Logger logger = LoggerFactory.getLogger(PatternCache.class);

    private final Map<ColumnType, List<Pattern>> patternCache = new HashMap<>();
    private final DetectionPatternRepository repository;

    public PatternCache(DetectionPatternRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initializePatterns() {
        reload();
    }

    /**
     * Recharge tous les patterns depuis la BDD et recompile le cache en mémoire.
     * Appel thread-safe — bloque jusqu'à la fin du rechargement.
     */
    public synchronized void reload() {
        logger.info("PatternCache: rechargement depuis la BDD...");
        patternCache.clear();

        List<DetectionPattern> all = repository.findAll();

        for (ColumnType columnType : ColumnType.values()) {
            String typeName = columnType.name();
            List<Pattern> compiled = all.stream()
                    .filter(p -> typeName.equals(p.getColumnType()))
                    .map(p -> {
                        try {
                            return Pattern.compile(p.getPattern());
                        } catch (Exception e) {
                            logger.warn("PatternCache: pattern invalide ignoré [{} / {}] — {}",
                                    typeName, p.getPattern(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!compiled.isEmpty()) {
                patternCache.put(columnType, compiled);
                logger.debug("PatternCache: {} patterns compilés pour {}", compiled.size(), typeName);
            }
        }

        logger.info("PatternCache: rechargé — {} types actifs, {} patterns au total",
                patternCache.size(),
                patternCache.values().stream().mapToInt(List::size).sum());
    }

    /**
     * Retourne les patterns compilés pour un type donné.
     *
     * @param columnType Le type de colonne
     * @return Liste de {@link Pattern} compilés, ou liste vide si aucun
     */
    public List<Pattern> getPatterns(ColumnType columnType) {
        return patternCache.getOrDefault(columnType, new ArrayList<>());
    }

    /**
     * Retourne tous les patterns du cache.
     *
     * @return Copie de la map interne
     */
    public Map<ColumnType, List<Pattern>> getAllPatterns() {
        return new HashMap<>(patternCache);
    }

    /**
     * Retourne le nombre de types actifs dans le cache.
     */
    public int getActiveTypeCount() {
        return patternCache.size();
    }
}
