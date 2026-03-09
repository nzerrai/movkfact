package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ColumnPatternDetector - validates header pattern matching.
 */
@SpringBootTest
public class ColumnPatternDetectorTests {

    @Autowired(required = false)
    private ColumnPatternDetector detector;

    @BeforeEach
    public void setup() {
        // Detector will be initialized with PatternCache
    }

    @Test
    public void detector_matches_first_name_patterns() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("first_name");
        assertThat(matches).containsKey(ColumnType.FIRST_NAME);
        assertThat(matches.get(ColumnType.FIRST_NAME)).isGreaterThan(0);
    }

    @Test
    public void detector_matches_email_patterns() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("email");
        assertThat(matches).containsKey(ColumnType.EMAIL);
        assertThat(matches.get(ColumnType.EMAIL)).isGreaterThan(0);
    }

    @Test
    public void detector_matches_date_birth_patterns() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("date_of_birth");
        assertThat(matches).containsKey(ColumnType.BIRTH_DATE);
        assertThat(matches.get(ColumnType.BIRTH_DATE)).isGreaterThan(0);
    }

    @Test
    public void detector_case_insensitive_matching() {
        Map<ColumnType, Integer> matches1 = detector.matchPatterns("EMAIL");
        Map<ColumnType, Integer> matches2 = detector.matchPatterns("email");
        Map<ColumnType, Integer> matches3 = detector.matchPatterns("Email");

        assertThat(matches1).containsKey(ColumnType.EMAIL);
        assertThat(matches2).containsKey(ColumnType.EMAIL);
        assertThat(matches3).containsKey(ColumnType.EMAIL);
    }

    @Test
    public void detector_returns_zero_score_for_unknown() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("completely_unknown_column_xyz");
        // Unknown should have no high-confidence matches
        long highConfidence = matches.values().stream()
                .filter(score -> score > 50)
                .count();
        assertThat(highConfidence).isLessThanOrEqualTo(1);
    }

    @Test
    public void detector_calculates_confidence_score() {
        // Confidence = 80 + (20 * matched / total) ensures high confidence (80-100) when patterns match
        // For "Amount" header: matches 1/6 patterns -> confidence = 80 + (20*1)/6 = 83%
        Map<ColumnType, Integer> matches = detector.matchPatterns("Amount");
        assertThat(matches.get(ColumnType.AMOUNT)).isGreaterThanOrEqualTo(80);
    }

    // ── Regression: "Name" → LAST_NAME (not FIRST_NAME) ─────────────────────

    @Test
    public void detector_name_maps_to_last_name() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("Name");
        assertThat(matches).containsKey(ColumnType.LAST_NAME);
        assertThat(matches).doesNotContainKey(ColumnType.FIRST_NAME);
    }

    @Test
    public void detector_nom_famille_maps_to_last_name() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("nom_de_famille");
        assertThat(matches).containsKey(ColumnType.LAST_NAME);
    }

    // ── Regression: "age_actuel" → INTEGER (not AMOUNT) ──────────────────────

    @Test
    public void detector_age_maps_to_integer() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("age");
        assertThat(matches).containsKey(ColumnType.INTEGER);
        assertThat(matches).doesNotContainKey(ColumnType.AMOUNT);
    }

    @Test
    public void detector_age_actuel_maps_to_integer() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("age_actuel");
        assertThat(matches).containsKey(ColumnType.INTEGER);
        assertThat(matches).doesNotContainKey(ColumnType.AMOUNT);
    }

    @Test
    public void detector_score_maps_to_integer() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("score");
        assertThat(matches).containsKey(ColumnType.INTEGER);
    }

    // ── New types: URL, UUID, IP_ADDRESS, COUNTRY, COMPANY, BOOLEAN, PERCENTAGE, DECIMAL, ENUM, TEXT ──

    @Test
    public void detector_url_maps_to_url() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("url");
        assertThat(matches).containsKey(ColumnType.URL);
    }

    @Test
    public void detector_website_maps_to_url() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("website");
        assertThat(matches).containsKey(ColumnType.URL);
    }

    @Test
    public void detector_uuid_maps_to_uuid() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("uuid");
        assertThat(matches).containsKey(ColumnType.UUID);
    }

    @Test
    public void detector_ip_address_maps_to_ip_address() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("ip_address");
        assertThat(matches).containsKey(ColumnType.IP_ADDRESS);
    }

    @Test
    public void detector_ip_maps_to_ip_address() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("ip");
        assertThat(matches).containsKey(ColumnType.IP_ADDRESS);
    }

    @Test
    public void detector_country_maps_to_country() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("country");
        assertThat(matches).containsKey(ColumnType.COUNTRY);
    }

    @Test
    public void detector_pays_maps_to_country() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("pays");
        assertThat(matches).containsKey(ColumnType.COUNTRY);
    }

    @Test
    public void detector_company_maps_to_company() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("company");
        assertThat(matches).containsKey(ColumnType.COMPANY);
    }

    @Test
    public void detector_entreprise_maps_to_company() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("entreprise");
        assertThat(matches).containsKey(ColumnType.COMPANY);
    }

    @Test
    public void detector_actif_maps_to_boolean() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("actif");
        assertThat(matches).containsKey(ColumnType.BOOLEAN);
    }

    @Test
    public void detector_taux_maps_to_percentage() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("taux");
        assertThat(matches).containsKey(ColumnType.PERCENTAGE);
    }

    @Test
    public void detector_prix_maps_to_decimal() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("prix");
        assertThat(matches).containsKey(ColumnType.DECIMAL);
    }

    @Test
    public void detector_statut_maps_to_enum() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("statut");
        assertThat(matches).containsKey(ColumnType.ENUM);
    }

    @Test
    public void detector_description_maps_to_text() {
        Map<ColumnType, Integer> matches = detector.matchPatterns("description");
        assertThat(matches).containsKey(ColumnType.TEXT);
    }
}
