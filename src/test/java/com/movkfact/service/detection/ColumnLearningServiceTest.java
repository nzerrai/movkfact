package com.movkfact.service.detection;

import com.movkfact.dto.ColumnFeedbackRequest;
import com.movkfact.dto.InferenceResult;
import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour ColumnLearningService (S10.1 + S10.2).
 * Couvre : normalisation, lookup (sous seuil / au seuil / au-dessus), ambiguïté,
 * enregistrement feedback, cycle complet, domain-scope (S10.2).
 */
@SpringBootTest
@Transactional
class ColumnLearningServiceTest {

    @Autowired
    private ColumnLearningService service;

    // ── Normalisation ────────────────────────────────────────────────────────

    @Test
    void normalize_accentAndSpaces_returnsSlug() {
        assertThat(service.normalize("Salaire Espéré")).isEqualTo("salaire-espere");
    }

    @Test
    void normalize_underscoresAndUpperCase_returnsSlug() {
        assertThat(service.normalize("First_Name")).isEqualTo("first-name");
    }

    @Test
    void normalize_alreadyClean_unchanged() {
        assertThat(service.normalize("email")).isEqualTo("email");
    }

    @Test
    void normalize_null_returnsEmpty() {
        assertThat(service.normalize(null)).isEmpty();
    }

    @Test
    void normalize_blankString_returnsEmpty() {
        assertThat(service.normalize("   ")).isEmpty();
    }

    // ── Lookup — sous le seuil ───────────────────────────────────────────────

    @Test
    void lookup_unknownName_returnsEmpty() {
        Optional<InferenceResult> result = service.lookup("colonne-inconnue-xyz", null);
        assertThat(result).isEmpty();
    }

    @Test
    void lookup_belowMinCount_returnsEmpty() {
        // Enregistrer 2 fois (min-count = 3 par défaut)
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("prenom_client", null, "FIRST_NAME")
        );
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);

        Optional<InferenceResult> result = service.lookup("prenom-client", null);
        assertThat(result).isEmpty(); // count=2 < 3
    }

    // ── Lookup — au seuil (count == 3) ──────────────────────────────────────

    @Test
    void lookup_atMinCount_returnsLearned() {
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("code_postal_client", null, "ZIP_CODE")
        );
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);

        Optional<InferenceResult> result = service.lookup("code-postal-client", null);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ColumnType.ZIP_CODE);
        assertThat(result.get().getLevel()).isEqualTo(InferenceLevel.LEARNED);
        assertThat(result.get().getConfidence()).isGreaterThanOrEqualTo(85.0);
    }

    // ── Lookup — au-dessus du seuil, confiance croissante ───────────────────

    @Test
    void lookup_aboveMinCount_higherConfidence() {
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("email_contact", null, "EMAIL")
        );
        // 5 validations
        for (int i = 0; i < 5; i++) {
            service.recordFeedback(feedbacks, null);
        }

        Optional<InferenceResult> result = service.lookup("email-contact", null);
        assertThat(result).isPresent();
        assertThat(result.get().getConfidence()).isGreaterThanOrEqualTo(90.0);
        assertThat(result.get().getConfidence()).isLessThanOrEqualTo(95.0); // plafond
    }

    // ── Ambiguïté : tie → pas de promotion ──────────────────────────────────

    @Test
    void lookup_ambiguousTie_returnsEmpty() {
        // 3 fois EMAIL, 3 fois PHONE pour le même nom → ambiguïté
        List<ColumnFeedbackRequest> emailFeedback = List.of(
                new ColumnFeedbackRequest("contact_info", null, "EMAIL")
        );
        List<ColumnFeedbackRequest> phoneFeedback = List.of(
                new ColumnFeedbackRequest("contact_info", null, "PHONE")
        );
        for (int i = 0; i < 3; i++) {
            service.recordFeedback(emailFeedback, null);
            service.recordFeedback(phoneFeedback, null);
        }

        Optional<InferenceResult> result = service.lookup("contact-info", null);
        assertThat(result).isEmpty();
    }

    // ── Cycle complet : feedback → infer via ColumnTypeInferenceService ──────

    @Autowired
    private ColumnTypeInferenceService inferenceService;

    @Test
    void fullCycle_feedbackThenInfer_returnsLearned() {
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("salaire espéré", null, "AMOUNT")
        );
        // 3 validations pour atteindre le seuil
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);

        InferenceResult result = inferenceService.infer("salaire espéré", List.of("35000", "45000"));
        assertThat(result.getType()).isEqualTo(ColumnType.AMOUNT);
        assertThat(result.getLevel()).isEqualTo(InferenceLevel.LEARNED);
    }

    // ── recordFeedback — edge cases ──────────────────────────────────────────

    @Test
    void recordFeedback_nullList_noException() {
        service.recordFeedback(null, 1L);
        // pas d'exception
    }

    @Test
    void recordFeedback_emptyValidatedType_ignored() {
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("col_test", null, "")
        );
        service.recordFeedback(feedbacks, null);
        assertThat(service.lookup("col-test", null)).isEmpty();
    }

    // ── Domain-scope (S10.2) ─────────────────────────────────────────────────

    @Test
    void lookup_domainMappingPrioritizedOverGlobal() {
        // AC1 canonical : le domaine doit primer sur un global qui a un vainqueur clair (pas un tie)
        // Setup : global EMAIL fort (5 fois via null), domaine 99 PHONE (3 fois)
        // recordFeedback(fb, 99L) écrit aussi en global → global final : EMAIL(5) + PHONE(3) → EMAIL gagne
        // Sans domain-first, lookup(99L) retournerait EMAIL. Avec domain-first, doit retourner PHONE.
        List<ColumnFeedbackRequest> globalFb = List.of(
                new ColumnFeedbackRequest("col_ac1", null, "EMAIL")
        );
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null); // EMAIL count=5 en global

        List<ColumnFeedbackRequest> domainFb = List.of(
                new ColumnFeedbackRequest("col_ac1", null, "PHONE")
        );
        service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(1)
        service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(2)
        service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(3) → EMAIL gagne clairement

        String normalized = service.normalize("col_ac1");

        // Global a un vainqueur clair : EMAIL(5) > PHONE(3)
        Optional<InferenceResult> globalResult = service.lookup(normalized, null);
        assertThat(globalResult).isPresent();
        assertThat(globalResult.get().getType()).isEqualTo(ColumnType.EMAIL);

        // Domaine 99 → PHONE (domain-first override le vainqueur global EMAIL)
        Optional<InferenceResult> domainResult = service.lookup(normalized, 99L);
        assertThat(domainResult).isPresent();
        assertThat(domainResult.get().getType()).isEqualTo(ColumnType.PHONE);
    }

    @Test
    void lookup_domainFeedbackPollutesGlobal_createsAmbiguity() {
        // Global EMAIL(3) + domaine 99 PHONE(3) → global tie → pas de promotion globale
        // Mais domaine 99 voit son PHONE sans ambiguïté
        List<ColumnFeedbackRequest> globalFb = List.of(
                new ColumnFeedbackRequest("col_tie", null, "EMAIL")
        );
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);

        List<ColumnFeedbackRequest> domainFb = List.of(
                new ColumnFeedbackRequest("col_tie", null, "PHONE")
        );
        service.recordFeedback(domainFb, 99L);
        service.recordFeedback(domainFb, 99L);
        service.recordFeedback(domainFb, 99L);

        String normalized = service.normalize("col_tie");

        // Lookup global (domainId=null) → tie → ambiguïté → pas de promotion
        assertThat(service.lookup(normalized, null)).isEmpty();

        // Lookup domaine 99 → PHONE (domain-first, pas de tie au niveau domaine)
        Optional<InferenceResult> domainResult = service.lookup(normalized, 99L);
        assertThat(domainResult).isPresent();
        assertThat(domainResult.get().getType()).isEqualTo(ColumnType.PHONE);
    }

    @Test
    void lookup_domainAbsent_fallsBackToGlobal() {
        // Global : zip_code → ZIP_CODE (3 fois via null, pas de pollution domaine)
        List<ColumnFeedbackRequest> globalFb = List.of(
                new ColumnFeedbackRequest("zip_code", null, "ZIP_CODE")
        );
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);
        service.recordFeedback(globalFb, null);

        String normalized = service.normalize("zip_code");

        // Lookup pour domaine 42 (aucun mapping domaine) → fallback global
        Optional<InferenceResult> result = service.lookup(normalized, 42L);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ColumnType.ZIP_CODE);
        assertThat(result.get().getLevel()).isEqualTo(InferenceLevel.LEARNED);
    }

    @Test
    void lookup_nullDomainId_usesGlobalOnly() {
        // Comportement V1 inchangé : domainId=null → global seulement
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("last_name", null, "LAST_NAME")
        );
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);

        String normalized = service.normalize("last_name");
        Optional<InferenceResult> result = service.lookup(normalized, null);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ColumnType.LAST_NAME);
    }

    @Test
    void lookup_noHttpContext_worksAsGlobalOnly() {
        // AC7 — appel direct avec null = comportement V1 = pas de régression batch/scheduled
        List<ColumnFeedbackRequest> feedbacks = List.of(
                new ColumnFeedbackRequest("ref_country", null, "COUNTRY")
        );
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);
        service.recordFeedback(feedbacks, null);

        String normalized = service.normalize("ref_country"); // → "ref-country"
        Optional<InferenceResult> result = service.lookup(normalized, null);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(ColumnType.COUNTRY);
        assertThat(result.get().getLevel()).isEqualTo(InferenceLevel.LEARNED);
    }

    @Test
    void lookup_domainIsolation_noLeakBetweenDomains() {
        // AC3 — les mappings domain-spécifiques de domaine 10 ne sont pas visibles par domaine 20
        String raw = "iso_col";

        List<ColumnFeedbackRequest> domain10Fb = List.of(
                new ColumnFeedbackRequest(raw, null, "PHONE")
        );
        service.recordFeedback(domain10Fb, 10L);
        service.recordFeedback(domain10Fb, 10L);
        service.recordFeedback(domain10Fb, 10L);

        List<ColumnFeedbackRequest> domain20Fb = List.of(
                new ColumnFeedbackRequest(raw, null, "AMOUNT")
        );
        service.recordFeedback(domain20Fb, 20L);
        service.recordFeedback(domain20Fb, 20L);
        service.recordFeedback(domain20Fb, 20L);

        String normalized = service.normalize(raw); // → "iso-col"

        // Domaine 10 → PHONE (son mapping domain-specific prime)
        Optional<InferenceResult> d10 = service.lookup(normalized, 10L);
        assertThat(d10).isPresent();
        assertThat(d10.get().getType()).isEqualTo(ColumnType.PHONE);

        // Domaine 20 → AMOUNT (son mapping domain-specific, pas celui de domaine 10)
        Optional<InferenceResult> d20 = service.lookup(normalized, 20L);
        assertThat(d20).isPresent();
        assertThat(d20.get().getType()).isEqualTo(ColumnType.AMOUNT);

        // Global : PHONE(3)+AMOUNT(3) = tie → ambiguïté → pas de promotion
        assertThat(service.lookup(normalized, null)).isEmpty();
    }
}
