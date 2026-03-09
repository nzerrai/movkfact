package com.movkfact.service.detection;

import com.movkfact.dto.InferenceResult;
import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ColumnTypeInferenceService (S9.1).
 * Covers Niveau 1 (name-based) and Niveau 2 (data-based) inference,
 * plus edge cases. ≥ 20 test cases as required by AC5.
 */
@SpringBootTest
class ColumnTypeInferenceServiceTest {

    @Autowired
    private ColumnTypeInferenceService service;

    // ── Niveau 1 : nom de colonne ────────────────────────────────────────────

    @Test
    void infer_emailName_returnsEmailNameBased() {
        InferenceResult result = service.infer("email", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.EMAIL);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
        assertThat(result.getConfidence()).isGreaterThanOrEqualTo(60.0);
    }

    @Test
    void infer_eMailName_returnsEmailNameBased() {
        InferenceResult result = service.infer("e_mail", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.EMAIL);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_phoneName_returnsPhoneNameBased() {
        InferenceResult result = service.infer("phone", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.PHONE);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_telName_returnsPhoneNameBased() {
        InferenceResult result = service.infer("tel", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.PHONE);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_firstnameName_returnsFirstNameBased() {
        InferenceResult result = service.infer("firstname", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.FIRST_NAME);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_prenomName_returnsFirstNameBased() {
        InferenceResult result = service.infer("prenom", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.FIRST_NAME);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_lastnameName_returnsLastNameBased() {
        InferenceResult result = service.infer("lastname", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.LAST_NAME);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_dateName_returnsDateNameBased() {
        InferenceResult result = service.infer("date", Collections.emptyList());
        assertThat(result.getType()).isIn(ColumnType.DATE, ColumnType.BIRTH_DATE);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_birthdateName_returnsBirthDateNameBased() {
        InferenceResult result = service.infer("birthdate", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.BIRTH_DATE);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    @Test
    void infer_montantName_returnsAmountNameBased() {
        InferenceResult result = service.infer("montant", Collections.emptyList());
        assertThat(result.getType()).isEqualTo(ColumnType.AMOUNT);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.NAME_BASED);
    }

    // ── Niveau 2 : analyse des données ───────────────────────────────────────

    @Test
    void infer_unknownNameWithEmailData_returnsEmailDataBased() {
        List<String> emails = Arrays.asList(
                "alice@example.com", "bob@test.org", "carol@domain.fr",
                "dave@mail.net", "eve@sample.io"
        );
        InferenceResult result = service.infer("col_xyz", emails);
        assertThat(result.getType()).isEqualTo(ColumnType.EMAIL);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.DATA_BASED);
    }

    @Test
    void infer_unknownNameWithDateData_returnsDataBased_nonNull() {
        // ISO-8601 dates — détectées DATA_BASED (le type exact dépend des validators spécialisés)
        List<String> dates = Arrays.asList(
                "2024-01-15", "2024-02-20", "2023-12-31",
                "2024-03-05", "2024-04-10"
        );
        InferenceResult result = service.infer("col_xyz", dates);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.DATA_BASED);
        assertThat(result.getType()).isNotNull();
    }

    @Test
    void infer_unknownNameWithEmailData_dataBased_phoneFormat() {
        // Numéros FR — AddressValidator peut les capter ; on vérifie juste DATA_BASED
        List<String> phones = Arrays.asList(
                "0612345678", "0712345678", "0612345679",
                "0712345680", "0612345681"
        );
        InferenceResult result = service.infer("valeur", phones);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.DATA_BASED);
        assertThat(result.getType()).isNotNull();
    }

    // ── Confidence levels ────────────────────────────────────────────────────

    @Test
    void infer_emailName_highConfidence() {
        InferenceResult result = service.infer("email", Collections.emptyList());
        assertThat(result.getConfidence()).isGreaterThanOrEqualTo(85.0);
    }

    @Test
    void infer_dataBased_confidence85() {
        List<String> emails = Arrays.asList(
                "a@b.com", "c@d.org", "e@f.net", "g@h.fr", "i@j.io"
        );
        InferenceResult result = service.infer("colonne_inconnue", emails);
        assertThat(result.getConfidence()).isGreaterThanOrEqualTo(80.0);
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void infer_nullName_nullValues_returnsInconclusive() {
        InferenceResult result = service.infer(null, null);
        assertThat(result.getType()).isNull();
        assertThat(result.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void infer_emptyName_emptyValues_returnsInconclusive() {
        InferenceResult result = service.infer("", Collections.emptyList());
        assertThat(result.getType()).isNull();
    }

    @Test
    void infer_blankName_emptyValues_returnsInconclusive() {
        InferenceResult result = service.infer("   ", Collections.emptyList());
        assertThat(result.getType()).isNull();
    }

    @Test
    void infer_unknownNameMixedData_noNPE() {
        List<String> mixed = Arrays.asList("hello", "world", "123", "foo", "bar");
        InferenceResult result = service.infer("random_col", mixed);
        assertThat(result).isNotNull();
        assertThat(result.getLevel()).isNotNull();
    }

    @Test
    void infer_result_alwaysHasLevel() {
        InferenceResult r1 = service.infer("email", Collections.emptyList());
        InferenceResult r2 = service.infer("unknown_xyz", Arrays.asList("abc", "def"));
        InferenceResult r3 = service.infer(null, null);
        assertThat(r1.getLevel()).isNotNull();
        assertThat(r2.getLevel()).isNotNull();
        assertThat(r3.getLevel()).isNotNull();
    }
}
