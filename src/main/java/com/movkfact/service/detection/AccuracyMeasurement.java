package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.dto.DetectedColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * AccuracyMeasurement - Framework pour mesurer la précision de la détection de types
 * 
 * Formule Accuracy:
 *   Pour chaque colonne détectée:
 *     - Si type détecté == type attendu: score = 1.0 (correct)
 *     - Si type dans alternatives acceptables: score = 0.5 (acceptable)
 *     - Sinon: score = 0.0 (incorrect)
 *   
 *   accuracy = (sum(scores) / totalColonnes) × 100
 * 
 * AC6 Cible: accuracy ≥ 85%
 * 
 * @since S2.2.4 Phase C
 */
@Service
public class AccuracyMeasurement {
    
    private static final Logger logger = LoggerFactory.getLogger(AccuracyMeasurement.class);
    
    /**
     * Mesurer accuracy sur ensemble de test
     * 
     * @param expectedResults Résultats attendus (vérité établie)
     * @param detectedResults Résultats de détection du système
     * @return AccuracyReport avec score et détails
     */
    public AccuracyReport measureAccuracy(
            List<ExpectedColumnType> expectedResults,
            TypeDetectionResult detectedResults) {
        
        logger.info("AccuracyMeasurement: Mesurant accuracy pour {} colonnes", 
                expectedResults.size());
        
        List<ColumnAccuracyResult> columnResults = new ArrayList<>();
        double totalScore = 0.0;
        
        // Pour chaque colonne attendue, comparer vs détecté
        for (ExpectedColumnType expected : expectedResults) {
            
            // Trouver la colonne détectée correspondante
            DetectedColumn detected = detectedResults.getColumns().stream()
                    .filter(col -> col.getColumnName().equalsIgnoreCase(expected.getColumnName()))
                    .findFirst()
                    .orElse(null);
            
            double score = compareTypes(expected, detected);
            totalScore += score;
            
            ColumnAccuracyResult result = new ColumnAccuracyResult(
                    expected.getColumnName(),
                    expected.getExpectedType(),
                    detected != null ? detected.getDetectedType() : null,
                    detected != null ? detected.getConfidence() : 0.0,
                    expected.getAlternatives(),
                    score
            );
            columnResults.add(result);
            
            logger.debug("AccuracyMeasurement: Column '{}' → Expected: {}, Detected: {}, Score: {}",
                    expected.getColumnName(),
                    expected.getExpectedType(),
                    detected != null ? detected.getDetectedType() : "NULL",
                    score);
        }
        
        // Calculer accuracy global
        double accuracyPercent = calculateAccuracy(columnResults);
        
        logger.info("AccuracyMeasurement: Accuracy global = {}% (cible: ≥85%)",
                String.format("%.2f", accuracyPercent));
        
        return new AccuracyReport(
                accuracyPercent,
                columnResults,
                accuracyPercent >= 85.0  // AC6 cible
        );
    }
    
    /**
     * Comparer type détecté vs type attendu
     * 
     * Formule:
     *   1.0 si détecté == attendu (correct)
     *   0.5 si détecté dans alternatives (acceptable)
     *   0.0 sinon (incorrect)
     * 
     * @param expected Type attendu avec alternatives
     * @param detected Colonne détectée
     * @return Score 0.0-1.0
     */
    private double compareTypes(ExpectedColumnType expected, DetectedColumn detected) {
        
        // Si pas de détection
        if (detected == null || detected.getDetectedType() == null) {
            logger.debug("  Column '{}': No detection → score 0.0", expected.getColumnName());
            return 0.0;
        }
        
        ColumnType expectedType = expected.getExpectedType();
        ColumnType detectedType = detected.getDetectedType();
        
        // Correspondance exacte
        if (detectedType == expectedType) {
            logger.debug("  Column '{}': {} == {} → CORRECT (score 1.0)", 
                    expected.getColumnName(), detectedType, expectedType);
            return 1.0;
        }
        
        // Correspondance dans alternatives
        if (expected.getAlternatives() != null && 
            expected.getAlternatives().contains(detectedType)) {
            logger.debug("  Column '{}': {} in alternatives {} → ACCEPTABLE (score 0.5)",
                    expected.getColumnName(), detectedType, expected.getAlternatives());
            return 0.5;
        }
        
        // Pas de correspondance
        logger.debug("  Column '{}': {} not in [{}, alternatives] → INCORRECT (score 0.0)",
                expected.getColumnName(), detectedType, expectedType);
        return 0.0;
    }
    
    /**
     * Calculer accuracy global
     * 
     * @param columnResults Résultats par colonne
     * @return Pourcentage accuracy (0-100)
     */
    private double calculateAccuracy(List<ColumnAccuracyResult> columnResults) {
        if (columnResults == null || columnResults.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = columnResults.stream()
                .mapToDouble(ColumnAccuracyResult::getScore)
                .sum();
        
        return (totalScore / columnResults.size()) * 100.0;
    }
    
    // ============ Data Classes ============
    
    /**
     * Résultat d'accuracy pour une colonne
     */
    public static class ColumnAccuracyResult {
        private String columnName;
        private ColumnType expectedType;
        private ColumnType detectedType;
        private Double detectedConfidence;
        private Set<ColumnType> alternatives;
        private double score;
        
        public ColumnAccuracyResult(String columnName, ColumnType expectedType,
                                   ColumnType detectedType, Double detectedConfidence,
                                   Set<ColumnType> alternatives, double score) {
            this.columnName = columnName;
            this.expectedType = expectedType;
            this.detectedType = detectedType;
            this.detectedConfidence = detectedConfidence;
            this.alternatives = alternatives;
            this.score = score;
        }
        
        public String getColumnName() { return columnName; }
        public ColumnType getExpectedType() { return expectedType; }
        public ColumnType getDetectedType() { return detectedType; }
        public Double getDetectedConfidence() { return detectedConfidence; }
        public Set<ColumnType> getAlternatives() { return alternatives; }
        public double getScore() { return score; }
        
        public String getStatus() {
            if (score == 1.0) return "✅ CORRECT";
            else if (score == 0.5) return "⚠️ ACCEPTABLE";
            else return "❌ INCORRECT";
        }
    }
    
    /**
     * Type de colonne attendu (vérité établie)
     */
    public static class ExpectedColumnType {
        private String columnName;
        private ColumnType expectedType;
        private Set<ColumnType> alternatives;
        
        public ExpectedColumnType(String columnName, ColumnType expectedType) {
            this.columnName = columnName;
            this.expectedType = expectedType;
            this.alternatives = new HashSet<>();
        }
        
        public ExpectedColumnType(String columnName, ColumnType expectedType, 
                                 Set<ColumnType> alternatives) {
            this.columnName = columnName;
            this.expectedType = expectedType;
            this.alternatives = alternatives != null ? alternatives : new HashSet<>();
        }
        
        public String getColumnName() { return columnName; }
        public ColumnType getExpectedType() { return expectedType; }
        public Set<ColumnType> getAlternatives() { return alternatives; }
        
        public void addAlternative(ColumnType type) {
            this.alternatives.add(type);
        }
    }
    
    /**
     * Rapport d'accuracy global
     */
    public static class AccuracyReport {
        private double accuracyPercent;
        private List<ColumnAccuracyResult> columnResults;
        private boolean satisfiesAC6;
        
        public AccuracyReport(double accuracyPercent, List<ColumnAccuracyResult> columnResults,
                            boolean satisfiesAC6) {
            this.accuracyPercent = accuracyPercent;
            this.columnResults = columnResults;
            this.satisfiesAC6 = satisfiesAC6;
        }
        
        public double getAccuracyPercent() { return accuracyPercent; }
        public List<ColumnAccuracyResult> getColumnResults() { return columnResults; }
        public boolean isSatisfied() { return satisfiesAC6; }
        
        public String getSummary() {
            long correctCount = columnResults.stream()
                    .filter(r -> r.getScore() == 1.0).count();
            long acceptableCount = columnResults.stream()
                    .filter(r -> r.getScore() == 0.5).count();
            long incorrectCount = columnResults.stream()
                    .filter(r -> r.getScore() == 0.0).count();
            
            return String.format(
                    "Accuracy: %.2f%% (AC6 target: ≥85%%) | " +
                    "Correct: %d, Acceptable: %d, Incorrect: %d | " +
                    "Status: %s",
                    accuracyPercent, correctCount, acceptableCount, incorrectCount,
                    satisfiesAC6 ? "✅ SATISFIED" : "❌ NOT SATISFIED"
            );
        }
    }
}
