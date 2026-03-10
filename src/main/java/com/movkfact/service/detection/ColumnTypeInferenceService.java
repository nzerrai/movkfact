package com.movkfact.service.detection;

import com.movkfact.context.DetectionContext;
import com.movkfact.dto.InferenceResult;
import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import com.movkfact.service.detection.financial.FinancialTypeDetector;
import com.movkfact.service.detection.personal.PersonalTypeDetector;
import com.movkfact.service.detection.temporal.TemporalTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Smart three-level column type inference service (S9.1 + S10.1).
 *
 * <p><b>Niveau 0 — Learned (S10.1):</b> Looks up the user-validated corpus via
 * {@link ColumnLearningService}. If a reliable mapping (count ≥ min-count) exists,
 * returns LEARNED result immediately.</p>
 *
 * <p><b>Niveau 1 — Name-based:</b> Uses {@link ColumnPatternDetector} to match the column
 * name against a dictionary of known patterns (email, phone, first_name, ...).
 * If best confidence ≥ {@value #NAME_CONFIDENCE_THRESHOLD}, returns NAME_BASED result.</p>
 *
 * <p><b>Niveau 2 — Data-based (fallback):</b> If name-based confidence is insufficient,
 * delegates to specialized detectors (Personal, Financial, Temporal) and
 * {@link ColumnValueAnalyzer}, returning DATA_BASED result.</p>
 *
 * <p>Confidence scale: 0–100. HIGH ≥ 85, MEDIUM 60–84, LOW &lt; 60.</p>
 */
@Service
public class ColumnTypeInferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ColumnTypeInferenceService.class);
    private static final double NAME_CONFIDENCE_THRESHOLD = 60.0;
    private static final double SPECIALIST_CONFIDENCE = 85.0;
    private static final double ANALYZER_CONFIDENCE = 80.0;

    @Autowired
    private ColumnLearningService learningService;

    @Autowired
    private ColumnPatternDetector patternDetector;

    @Autowired
    private PersonalTypeDetector personalTypeDetector;

    @Autowired
    private FinancialTypeDetector financialTypeDetector;

    @Autowired
    private TemporalTypeDetector temporalTypeDetector;

    @Autowired
    private ColumnValueAnalyzer valueAnalyzer;

    @Autowired(required = false) // null hors contexte HTTP (tests, batch)
    private DetectionContext detectionContext;

    /**
     * Infer the most likely column type using a three-level strategy.
     *
     * @param columnName   The CSV column header name (may be null)
     * @param sampleValues Up to 100 sample values from the column (may be null or empty)
     * @return {@link InferenceResult} with detected type, confidence, and inference level.
     *         type may be null if detection is inconclusive.
     */
    public InferenceResult infer(String columnName, List<String> sampleValues) {

        // ── Niveau 0 : corpus appris (S10.2 domain-first) ────────────────────────
        if (columnName != null && !columnName.isBlank()) {
            String normalized = learningService.normalize(columnName);
            Long currentDomainId = (detectionContext != null) ? detectionContext.getDomainId() : null;
            Optional<InferenceResult> learned = learningService.lookup(normalized, currentDomainId);
            if (learned.isPresent()) {
                logger.debug("ColumnTypeInferenceService: '{}' → {} (LEARNED, conf={})",
                        columnName, learned.get().getType(), Math.round(learned.get().getConfidence()));
                return learned.get();
            }
        }

        // ── Niveau 1 : nom de colonne ─────────────────────────────────────────────
        if (columnName != null && !columnName.isBlank()) {
            Map<ColumnType, Integer> nameMatches = patternDetector.matchPatterns(columnName);
            if (!nameMatches.isEmpty()) {
                Map.Entry<ColumnType, Integer> best = nameMatches.entrySet().stream()
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .orElse(null);

                if (best != null && best.getValue() >= NAME_CONFIDENCE_THRESHOLD) {
                    logger.debug("ColumnTypeInferenceService: '{}' → {} (NAME_BASED, conf={})",
                            columnName, best.getKey(), best.getValue());
                    return new InferenceResult(best.getKey(), best.getValue().doubleValue(),
                            InferenceLevel.NAME_BASED);
                }
            }
        }

        // ── Niveau 2 : analyse des données ───────────────────────────────────────
        if (sampleValues != null && !sampleValues.isEmpty()) {
            // Personal types (FIRST_NAME, LAST_NAME, EMAIL, PHONE, GENDER, ADDRESS)
            ColumnType personal = personalTypeDetector.detect(columnName, sampleValues);
            if (personal != null) {
                logger.debug("ColumnTypeInferenceService: '{}' → {} (DATA_BASED/personal)", columnName, personal);
                return new InferenceResult(personal, SPECIALIST_CONFIDENCE, InferenceLevel.DATA_BASED);
            }

            // Financial types (AMOUNT, ACCOUNT_NUMBER, CURRENCY)
            ColumnType financial = financialTypeDetector.detect(columnName, sampleValues);
            if (financial != null) {
                logger.debug("ColumnTypeInferenceService: '{}' → {} (DATA_BASED/financial)", columnName, financial);
                return new InferenceResult(financial, SPECIALIST_CONFIDENCE, InferenceLevel.DATA_BASED);
            }

            // Temporal types (BIRTH_DATE, DATE, TIME, TIMEZONE)
            ColumnType temporal = temporalTypeDetector.detect(columnName, sampleValues);
            if (temporal != null) {
                logger.debug("ColumnTypeInferenceService: '{}' → {} (DATA_BASED/temporal)", columnName, temporal);
                return new InferenceResult(temporal, SPECIALIST_CONFIDENCE, InferenceLevel.DATA_BASED);
            }

            // Generic value analysis (EMAIL, AMOUNT, DATE, ACCOUNT_NUMBER, TIMEZONE)
            ColumnType analyzed = valueAnalyzer.analyzeValues(columnName, sampleValues);
            if (analyzed != null) {
                logger.debug("ColumnTypeInferenceService: '{}' → {} (DATA_BASED/value-analyzer)", columnName, analyzed);
                return new InferenceResult(analyzed, ANALYZER_CONFIDENCE, InferenceLevel.DATA_BASED);
            }
        }

        logger.debug("ColumnTypeInferenceService: '{}' → inconclusive", columnName);
        return new InferenceResult(null, 0.0, InferenceLevel.NAME_BASED);
    }
}
