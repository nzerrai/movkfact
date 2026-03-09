package com.movkfact.service.detection;

import com.movkfact.entity.DetectionPattern;
import com.movkfact.repository.DetectionPatternRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Initialise la table {@code detection_pattern} depuis {@code patterns.yml}
 * si et seulement si elle est vide au démarrage.
 *
 * <p><b>Cas d'usage :</b></p>
 * <ul>
 *   <li><b>Production (Flyway activé) :</b> V010 a déjà seedé la table — ce composant
 *       détecte que la table est non-vide et ne fait rien.</li>
 *   <li><b>Dev / Tests (H2, Flyway désactivé) :</b> la table est vide au premier démarrage.
 *       Ce composant charge {@code patterns.yml} et insère tous les patterns,
 *       puis recharge le {@link PatternCache}.</li>
 * </ul>
 *
 * <p>Ce composant s'exécute après {@code ApplicationReadyEvent} pour être certain
 * que JPA et Flyway (s'il est actif) ont terminé leur initialisation.</p>
 */
@Component
@Order(0)
public class DetectionPatternInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DetectionPatternInitializer.class);

    private final DetectionPatternRepository repository;
    private final PatternCache patternCache;

    public DetectionPatternInitializer(DetectionPatternRepository repository,
                                       PatternCache patternCache) {
        this.repository = repository;
        this.patternCache = patternCache;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedIfEmpty() {
        // Utilise FIRST_NAME comme canari : présent ssi le seed YAML (ou V010 Flyway) a déjà tourné.
        // Évite de re-seeder si seuls des patterns de test isolés existent en BDD.
        boolean alreadySeeded = !repository.findByColumnType("FIRST_NAME").isEmpty();
        if (alreadySeeded) {
            logger.info("DetectionPatternInitializer: patterns YAML déjà présents — seed ignoré");
            return;
        }

        logger.info("DetectionPatternInitializer: table vide — chargement depuis patterns.yml");

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("patterns.yml")) {
            if (is == null) {
                logger.warn("DetectionPatternInitializer: patterns.yml introuvable sur le classpath");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, List<String>> data = new Yaml().load(is);

            int total = 0;
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                String columnType = entry.getKey();
                List<String> patterns = entry.getValue();
                if (patterns == null) continue;
                for (String pattern : patterns) {
                    repository.save(new DetectionPattern(columnType, pattern, null));
                    total++;
                }
            }

            logger.info("DetectionPatternInitializer: {} patterns insérés depuis patterns.yml", total);

            // Recharge le cache maintenant que la table est peuplée
            patternCache.reload();

        } catch (Exception e) {
            logger.error("DetectionPatternInitializer: erreur lors du seed depuis patterns.yml", e);
        }
    }
}
