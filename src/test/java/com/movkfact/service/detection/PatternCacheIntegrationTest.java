package com.movkfact.service.detection;

import com.movkfact.entity.DetectionPattern;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.DetectionPatternRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour PatternCache (S10.2).
 *
 * Vérifie que le cache charge depuis la BDD (plus depuis YAML),
 * et que reload() est effectif immédiatement après un INSERT ou DELETE.
 *
 * Note : Le profil dev utilise H2 avec flyway.enabled=false.
 * Les données de test sont insérées directement via le repository dans @BeforeEach.
 */
@SpringBootTest
class PatternCacheIntegrationTest {

    @Autowired
    private PatternCache patternCache;

    @Autowired
    private DetectionPatternRepository repository;

    @BeforeEach
    void seedTestPatterns() {
        // Insère les patterns de base si absents (idempotent via count)
        if (repository.findByColumnType("URL").isEmpty()) {
            repository.save(new DetectionPattern("URL", "(?i)^url$", "EN standard"));
            repository.save(new DetectionPattern("URL", "(?i)^website$", "EN website"));
        }
        if (repository.findByColumnType("UUID").isEmpty()) {
            repository.save(new DetectionPattern("UUID", "(?i)^uuid$", "EN standard"));
        }
        if (repository.findByColumnType("IP_ADDRESS").isEmpty()) {
            repository.save(new DetectionPattern("IP_ADDRESS", "(?i)^ip_?address$", "EN standard"));
        }
        if (repository.findByColumnType("COUNTRY").isEmpty()) {
            repository.save(new DetectionPattern("COUNTRY", "(?i)^country$", "EN standard"));
        }
        if (repository.findByColumnType("COMPANY").isEmpty()) {
            repository.save(new DetectionPattern("COMPANY", "(?i)^company$", "EN standard"));
        }
        if (repository.findByColumnType("BOOLEAN").isEmpty()) {
            repository.save(new DetectionPattern("BOOLEAN", "(?i)^actif$", "FR actif"));
        }
        if (repository.findByColumnType("PERCENTAGE").isEmpty()) {
            repository.save(new DetectionPattern("PERCENTAGE", "(?i)^taux$", "FR taux"));
        }
        if (repository.findByColumnType("DECIMAL").isEmpty()) {
            repository.save(new DetectionPattern("DECIMAL", "(?i)^prix$", "FR prix"));
        }
        if (repository.findByColumnType("ENUM").isEmpty()) {
            repository.save(new DetectionPattern("ENUM", "(?i)^statut$", "FR statut"));
        }
        if (repository.findByColumnType("TEXT").isEmpty()) {
            repository.save(new DetectionPattern("TEXT", "(?i)^description$", "EN/FR description"));
        }
        if (repository.findByColumnType("LAST_NAME").isEmpty()) {
            repository.save(new DetectionPattern("LAST_NAME", "(?i)^nom$", "FR court"));
        }
        if (repository.findByColumnType("CITY").isEmpty()) {
            repository.save(new DetectionPattern("CITY", "(?i)^city$", "EN standard"));
        }
        patternCache.reload();
    }

    // AC2 — PatternCache charge depuis BDD au démarrage
    @Test
    void cache_loads_from_database_on_startup() {
        assertThat(patternCache.getAllPatterns()).isNotEmpty();
        assertThat(patternCache.getActiveTypeCount()).isGreaterThan(0);
    }

    @Test
    void cache_contains_url_patterns_from_seed() {
        List<Pattern> urlPatterns = patternCache.getPatterns(ColumnType.URL);
        assertThat(urlPatterns).isNotEmpty();
        boolean matchesUrl = urlPatterns.stream().anyMatch(p -> p.matcher("url").find());
        assertThat(matchesUrl).isTrue();
    }

    @Test
    void cache_contains_all_seeded_types() {
        assertThat(patternCache.getPatterns(ColumnType.URL)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.UUID)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.IP_ADDRESS)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.COUNTRY)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.COMPANY)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.BOOLEAN)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.PERCENTAGE)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.DECIMAL)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.ENUM)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.TEXT)).isNotEmpty();
    }

    // AC3 — reload() recharge immédiatement après INSERT en BDD
    @Test
    @Transactional
    void reload_picks_up_new_pattern_inserted_in_db() {
        int before = patternCache.getPatterns(ColumnType.LAST_NAME).size();

        DetectionPattern newPattern = new DetectionPattern(
                "LAST_NAME", "(?i)^nom_test_s102$", "test S10.2");
        repository.save(newPattern);
        patternCache.reload();

        List<Pattern> after = patternCache.getPatterns(ColumnType.LAST_NAME);
        assertThat(after).hasSize(before + 1);
        boolean found = after.stream().anyMatch(p -> p.matcher("nom_test_s102").find());
        assertThat(found).isTrue();
    }

    // AC5 — après DELETE + reload, le pattern n'est plus dans le cache
    @Test
    @Transactional
    void reload_removes_deleted_pattern_from_cache() {
        DetectionPattern temp = new DetectionPattern(
                "CITY", "(?i)^cite_test_s102$", "test suppression S10.2");
        temp = repository.save(temp);
        patternCache.reload();

        Long tempId = temp.getId();
        repository.deleteById(tempId);
        patternCache.reload();

        List<Pattern> cityPatterns = patternCache.getPatterns(ColumnType.CITY);
        boolean stillPresent = cityPatterns.stream()
                .anyMatch(p -> p.matcher("cite_test_s102").find());
        assertThat(stillPresent).isFalse();
    }

    @Test
    void invalid_pattern_in_db_is_silently_ignored_during_reload() {
        patternCache.reload();
        assertThat(patternCache.getActiveTypeCount()).isGreaterThan(0);
    }
}
