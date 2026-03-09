package com.movkfact.dto;

import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;
import com.movkfact.enums.PiiCategory;
import java.util.List;

/**
 * DTO representing a single detected column.
 * S9.1: inferenceLevel (NAME_BASED | DATA_BASED)
 * S9.2: isPII + piiCategory for RGPD pre-fill
 */
public class DetectedColumn {
    private String columnName;
    private ColumnType detectedType;
    private Double confidence; // 0-100
    private List<ColumnType> alternatives;
    private List<String> matchedPatterns;
    private InferenceLevel inferenceLevel; // S9.1
    private boolean isPII;                 // S9.2
    private PiiCategory piiCategory;       // S9.2

    public DetectedColumn(String columnName, ColumnType detectedType, Double confidence) {
        this.columnName = columnName;
        this.detectedType = detectedType;
        this.confidence = confidence;
    }

    public DetectedColumn(String columnName, ColumnType detectedType, Double confidence,
                         List<ColumnType> alternatives, List<String> matchedPatterns) {
        this(columnName, detectedType, confidence);
        this.alternatives = alternatives;
        this.matchedPatterns = matchedPatterns;
    }

    // Getters & Setters
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public ColumnType getDetectedType() { return detectedType; }
    public void setDetectedType(ColumnType detectedType) { this.detectedType = detectedType; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public List<ColumnType> getAlternatives() { return alternatives; }
    public void setAlternatives(List<ColumnType> alternatives) { this.alternatives = alternatives; }

    public List<String> getMatchedPatterns() { return matchedPatterns; }
    public void setMatchedPatterns(List<String> matchedPatterns) { this.matchedPatterns = matchedPatterns; }

    public InferenceLevel getInferenceLevel() { return inferenceLevel; }
    public void setInferenceLevel(InferenceLevel inferenceLevel) { this.inferenceLevel = inferenceLevel; }

    public boolean isPII() { return isPII; }
    public void setPII(boolean isPII) { this.isPII = isPII; }

    public PiiCategory getPiiCategory() { return piiCategory; }
    public void setPiiCategory(PiiCategory piiCategory) { this.piiCategory = piiCategory; }
}
