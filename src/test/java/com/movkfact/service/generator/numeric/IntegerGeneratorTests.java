package com.movkfact.service.generator.numeric;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for IntegerGenerator — min/max boundary enforcement.
 */
class IntegerGeneratorTests {

    private ColumnConfigDTO config(Integer min, Integer max) {
        ColumnConfigDTO dto = new ColumnConfigDTO("col", ColumnType.INTEGER);
        dto.setMinValue(min);
        dto.setMaxValue(max);
        return dto;
    }

    @Test
    void generate_withMinValue1_MaxValue150_staysInRange() {
        IntegerGenerator gen = new IntegerGenerator(config(1, 150));
        for (int i = 0; i < 500; i++) {
            int val = (int) gen.generate();
            assertThat(val).isBetween(1, 150);
        }
    }

    @Test
    void generate_noConstraints_usesDefaults() {
        IntegerGenerator gen = new IntegerGenerator(config(null, null));
        int val = (int) gen.generate();
        assertThat(val).isBetween(0, 1000);
    }

    @Test
    void generate_minOnly_clampsLowerBound() {
        IntegerGenerator gen = new IntegerGenerator(config(50, null));
        for (int i = 0; i < 200; i++) {
            assertThat((int) gen.generate()).isGreaterThanOrEqualTo(50);
        }
    }

    @Test
    void generate_maxOnly_clampsUpperBound() {
        IntegerGenerator gen = new IntegerGenerator(config(null, 10));
        for (int i = 0; i < 200; i++) {
            assertThat((int) gen.generate()).isLessThanOrEqualTo(10);
        }
    }

    @Test
    void generate_minEqualsMax_returnsExactValue() {
        IntegerGenerator gen = new IntegerGenerator(config(42, 42));
        for (int i = 0; i < 20; i++) {
            assertThat((int) gen.generate()).isEqualTo(42);
        }
    }

    @Test
    void generate_constraintsMapOverridesFields() {
        ColumnConfigDTO dto = config(1, 150);
        dto.setConstraints(Map.of("min", 200, "max", 300));
        IntegerGenerator gen = new IntegerGenerator(dto);
        for (int i = 0; i < 200; i++) {
            assertThat((int) gen.generate()).isBetween(200, 300);
        }
    }
}
