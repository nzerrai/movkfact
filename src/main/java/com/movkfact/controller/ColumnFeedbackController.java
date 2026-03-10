package com.movkfact.controller;

import com.movkfact.dto.ColumnFeedbackRequest;
import com.movkfact.service.detection.ColumnLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pour l'enregistrement du feedback utilisateur (S10.1).
 *
 * <p>Endpoint : {@code POST /api/domains/{domainId}/feedback}</p>
 * <p>Reçoit la liste des colonnes avec leur type détecté et le type validé par l'utilisateur.</p>
 */
@RestController
@RequestMapping("/api/domains")
@Tag(name = "Column Learning", description = "Adaptive learning feedback API")
public class ColumnFeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(ColumnFeedbackController.class);

    @Autowired
    private ColumnLearningService learningService;

    /**
     * Enregistre le feedback de détection de types après génération.
     *
     * @param domainId  Identifiant du domaine courant
     * @param feedbacks Liste des feedbacks colonnes [{colName, detectedType, validatedType}]
     * @return 200 OK avec le nombre d'entrées enregistrées
     */
    @PostMapping("/{domainId}/feedback")
    @Operation(summary = "Enregistrer le feedback de détection de types",
               description = "Appris automatiquement après chaque génération de dataset")
    public ResponseEntity<Map<String, Object>> recordFeedback(
            @PathVariable Long domainId,
            @RequestBody List<ColumnFeedbackRequest> feedbacks) {

        logger.info("ColumnFeedbackController: received {} feedback entries for domain {}",
                feedbacks == null ? 0 : feedbacks.size(), domainId);

        learningService.recordFeedback(feedbacks, domainId);

        int count = feedbacks == null ? 0 : (int) feedbacks.stream()
                .filter(f -> f.getValidatedType() != null && !f.getValidatedType().isBlank())
                .count();

        return ResponseEntity.ok(Map.of(
                "recorded", count,
                "domainId", domainId
        ));
    }
}
