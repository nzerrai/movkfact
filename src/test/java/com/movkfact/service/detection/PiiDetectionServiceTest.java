package com.movkfact.service.detection;

import com.movkfact.dto.PiiResult;
import com.movkfact.enums.PiiCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour PiiDetectionService (S9.2).
 * ≥ 15 cas : patterns CONTACT, IDENTITY, LOCATION, faux positifs, edge cases.
 */
@SpringBootTest
class PiiDetectionServiceTest {

    @Autowired
    private PiiDetectionService service;

    // ── CONTACT ─────────────────────────────────────────────────────────────

    @Test
    void detect_emailName_isPii_CONTACT() {
        PiiResult result = service.detect("email", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.CONTACT);
    }

    @Test
    void detect_phoneName_isPii_CONTACT() {
        PiiResult result = service.detect("phone", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.CONTACT);
    }

    @Test
    void detect_telName_isPii_CONTACT() {
        PiiResult result = service.detect("tel", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.CONTACT);
    }

    // ── IDENTITY ─────────────────────────────────────────────────────────────

    @Test
    void detect_firstnameName_isPii_IDENTITY() {
        PiiResult result = service.detect("firstname", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.IDENTITY);
    }

    @Test
    void detect_lastnameName_isPii_IDENTITY() {
        PiiResult result = service.detect("lastname", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.IDENTITY);
    }

    @Test
    void detect_prenomName_isPii_IDENTITY() {
        PiiResult result = service.detect("prenom", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.IDENTITY);
    }

    @Test
    void detect_ibanData_isPii_IDENTITY() {
        List<String> ibans = Arrays.asList(
                "FR7630006000011234567890189",
                "FR7630006000011234567890189",
                "FR7630006000011234567890189"
        );
        PiiResult result = service.detect("compte_bancaire", ibans);
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.IDENTITY);
    }

    @Test
    void detect_siretData_isPii_IDENTITY() {
        List<String> sirets = Arrays.asList(
                "73282932000074", "73282932000074", "73282932000074"
        );
        PiiResult result = service.detect("identifiant_entreprise", sirets);
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.IDENTITY);
    }

    // ── LOCATION ─────────────────────────────────────────────────────────────

    @Test
    void detect_zipCodeName_isPii_LOCATION() {
        PiiResult result = service.detect("zip_code", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.LOCATION);
    }

    @Test
    void detect_cityName_isPii_LOCATION() {
        PiiResult result = service.detect("city", Collections.emptyList());
        assertThat(result.isPii()).isTrue();
        assertThat(result.getCategory()).isEqualTo(PiiCategory.LOCATION);
    }

    // ── Non-PII ──────────────────────────────────────────────────────────────

    @Test
    void detect_amountName_notPii() {
        PiiResult result = service.detect("montant", Collections.emptyList());
        assertThat(result.isPii()).isFalse();
        assertThat(result.getCategory()).isNull();
    }

    @Test
    void detect_dateName_notPii() {
        PiiResult result = service.detect("date", Collections.emptyList());
        assertThat(result.isPii()).isFalse();
    }

    @Test
    void detect_genericNumericData_notPii() {
        List<String> nums = Arrays.asList("100", "200", "300", "400", "500");
        PiiResult result = service.detect("valeur", nums);
        assertThat(result.isPii()).isFalse();
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    void detect_nullName_nullValues_noException() {
        PiiResult result = service.detect(null, null);
        assertThat(result).isNotNull();
        assertThat(result.isPii()).isFalse();
    }

    @Test
    void detect_emptyValues_noException() {
        PiiResult result = service.detect("colonne", Collections.emptyList());
        assertThat(result).isNotNull();
        assertThat(result.getDetectedBy()).isNotNull();
    }

    @Test
    void detect_result_alwaysHasDetectedBy() {
        PiiResult r1 = service.detect("email", Collections.emptyList());
        PiiResult r2 = service.detect("random", Arrays.asList("abc", "def"));
        PiiResult r3 = service.detect(null, null);
        assertThat(r1.getDetectedBy()).isNotNull();
        assertThat(r2.getDetectedBy()).isNotNull();
        assertThat(r3.getDetectedBy()).isNotNull();
    }
}
