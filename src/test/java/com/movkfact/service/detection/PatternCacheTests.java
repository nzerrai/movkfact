package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PatternCache - validates regex compilation and performance optimization.
 * WINSTON RECOMMENDATION: PatternCache singleton for ~100x perf boost
 */
@SpringBootTest
public class PatternCacheTests {

    @Autowired(required = false)
    private PatternCache patternCache;

    @BeforeEach
    public void setup() {
        // PatternCache will be initialized on first use
    }

    @Test
    public void patternCache_loads_all_patterns_at_startup() {
        assertThat(patternCache).isNotNull();
        assertThat(patternCache.getPatterns(ColumnType.FIRST_NAME))
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    public void patternCache_compilesPatterns_for_all_personal_types() {
        assertThat(patternCache.getPatterns(ColumnType.FIRST_NAME)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.LAST_NAME)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.EMAIL)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.GENDER)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.PHONE)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.ADDRESS)).isNotEmpty();
    }

    @Test
    public void patternCache_compilesPatterns_for_all_financial_types() {
        assertThat(patternCache.getPatterns(ColumnType.AMOUNT)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.ACCOUNT_NUMBER)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.CURRENCY)).isNotEmpty();
    }

    @Test
    public void patternCache_compilesPatterns_for_all_temporal_types() {
        assertThat(patternCache.getPatterns(ColumnType.DATE)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.TIME)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.TIMEZONE)).isNotEmpty();
        assertThat(patternCache.getPatterns(ColumnType.BIRTH_DATE)).isNotEmpty();
    }

    @Test
    public void patternCache_returns_compiled_patterns() {
        List<Pattern> patterns = patternCache.getPatterns(ColumnType.EMAIL);
        assertThat(patterns).allSatisfy(p -> 
            assertThat(p).isNotNull()
        );
    }

    @Test
    public void patternCache_caches_patterns_across_calls() {
        List<Pattern> patterns1 = patternCache.getPatterns(ColumnType.FIRST_NAME);
        List<Pattern> patterns2 = patternCache.getPatterns(ColumnType.FIRST_NAME);
        
        // Should return same cached instance
        assertThat(patterns1).isSameAs(patterns2);
    }
}
