package com.movkfact.service.detection;

import com.movkfact.dto.InferenceResult;
import com.movkfact.dto.PiiResult;
import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import com.movkfact.enums.PiiCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects personally identifiable information (PII) in CSV columns (S9.2).
 *
 * <p>Relies on {@link ColumnTypeInferenceService} for type inference, then maps
 * the inferred type to a {@link PiiCategory}. For columns with inconclusive type,
 * also scans sample values against IBAN, NIR (numéro sécu) and SIRET patterns.</p>
 *
 * <p>PII categories:</p>
 * <ul>
 *   <li>CONTACT  — EMAIL, PHONE</li>
 *   <li>IDENTITY — FIRST_NAME, LAST_NAME + data patterns IBAN/NIR/SIRET</li>
 *   <li>LOCATION — POSTAL_CODE, CITY</li>
 * </ul>
 */
@Service
public class PiiDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(PiiDetectionService.class);

    private static final Pattern IBAN_PATTERN =
            Pattern.compile("[A-Z]{2}\\d{2}[A-Z0-9]{4}\\d{7}([A-Z0-9]?){0,16}");
    private static final Pattern NIR_PATTERN =
            Pattern.compile("[12]\\d{2}(0[1-9]|1[0-2])\\d{5}\\d{3}\\d{2}");
    private static final Pattern SIRET_PATTERN =
            Pattern.compile("\\d{14}");

    /** Minimum fraction of values matching a data pattern to trigger PII flag */
    private static final double DATA_MATCH_THRESHOLD = 0.5;

    @Autowired
    private ColumnTypeInferenceService inferenceService;

    /**
     * Detect PII in a CSV column.
     *
     * @param columnName   Column header name
     * @param sampleValues Up to 100 sample values
     * @return {@link PiiResult} — always non-null
     */
    public PiiResult detect(String columnName, List<String> sampleValues) {
        // 1. Scan specific data patterns first (IBAN/NIR/SIRET are very specific, avoid misclassification)
        if (sampleValues != null && !sampleValues.isEmpty()) {
            PiiCategory dataCategory = scanDataPatterns(sampleValues);
            if (dataCategory != null) {
                logger.info("PII_DETECTED | column={} | category={} | detectedBy=DATA_BASED",
                        columnName, dataCategory);
                return new PiiResult(true, dataCategory, InferenceLevel.DATA_BASED);
            }
        }

        // 2. Fall back to type inference (name-based then value-based)
        InferenceResult inferred = inferenceService.infer(columnName, sampleValues);
        PiiCategory category = resolveCategory(inferred.getType());
        boolean isPii = category != null;

        if (isPii) {
            logger.info("PII_DETECTED | column={} | category={} | detectedBy={}",
                    columnName, category, inferred.getLevel());
        }

        return new PiiResult(isPii, category, inferred.getLevel());
    }

    private PiiCategory resolveCategory(ColumnType type) {
        if (type == null) return null;
        return switch (type) {
            case EMAIL, PHONE -> PiiCategory.CONTACT;
            case FIRST_NAME, LAST_NAME -> PiiCategory.IDENTITY;
            case ZIP_CODE, CITY, ADDRESS -> PiiCategory.LOCATION;
            default -> null;
        };
    }

    private PiiCategory scanDataPatterns(List<String> values) {
        long total = values.stream().filter(v -> v != null && !v.isBlank()).count();
        if (total == 0) return null;

        long ibanCount = values.stream()
                .filter(v -> v != null && IBAN_PATTERN.matcher(v.trim()).matches()).count();
        if ((double) ibanCount / total >= DATA_MATCH_THRESHOLD) return PiiCategory.IDENTITY;

        long nirCount = values.stream()
                .filter(v -> v != null && NIR_PATTERN.matcher(v.trim().replaceAll("\\s", "")).matches()).count();
        if ((double) nirCount / total >= DATA_MATCH_THRESHOLD) return PiiCategory.IDENTITY;

        long siretCount = values.stream()
                .filter(v -> v != null && SIRET_PATTERN.matcher(v.trim().replaceAll("\\s", "")).matches()).count();
        if ((double) siretCount / total >= DATA_MATCH_THRESHOLD) return PiiCategory.IDENTITY;

        return null;
    }
}
