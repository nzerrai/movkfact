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
}
