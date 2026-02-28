package com.movkfact.dto;

import com.movkfact.enums.ColumnType;
import java.util.List;

/**
 * DTO representing a single detected column.
 */
public class DetectedColumn {
    private String columnName;
    private ColumnType detectedType;
    private Double confidence; // 0-100
    private List<ColumnType> alternatives;
    private List<String> matchedPatterns;

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
}
