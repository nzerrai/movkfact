package com.movkfact.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests RGPD : GENDER doit toujours produire une valeur de genre valide.
 */
@SpringBootTest
class AnonymizationServiceGenderTest {

    @Autowired
    private AnonymizationService service;

    @Test
    void gender_mf_format_returns_valid_gender() {
        for (int i = 0; i < 50; i++) {
            String result = service.anonymizeValue("M", "GENDER");
            assertThat(result).isIn("M", "F");
            assertThat(result).isNotBlank();
        }
    }

    @Test
    void gender_hommefemme_format_preserved() {
        for (int i = 0; i < 50; i++) {
            String result = service.anonymizeValue("Homme", "GENDER");
            assertThat(result).isIn("Homme", "Femme");
        }
    }

    @Test
    void gender_malefemale_format_preserved() {
        for (int i = 0; i < 50; i++) {
            String result = service.anonymizeValue("Female", "GENDER");
            assertThat(result).isIn("Male", "Female");
        }
    }

    @Test
    void gender_null_value_returns_valid_gender() {
        String result = service.anonymizeValue(null, "GENDER");
        assertThat(result).isIn("M", "F");
        assertThat(result).isNotBlank();
    }

    @Test
    void gender_blank_value_returns_valid_gender() {
        String result = service.anonymizeValue("   ", "GENDER");
        assertThat(result).isIn("M", "F");
        assertThat(result).isNotBlank();
    }

    @Test
    void gender_unknown_value_returns_mf() {
        String result = service.anonymizeValue("inconnu", "GENDER");
        assertThat(result).isIn("M", "F");
        assertThat(result).isNotBlank();
    }
}
