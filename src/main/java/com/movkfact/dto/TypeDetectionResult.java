package com.movkfact.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for CSV Type Detection API response.
 * Contains detected columns with confidence scores and detection metadata.
 */
public class TypeDetectionResult {
    private List<DetectedColumn> columns;
    private Map<String, String> statistics;
    private String detectionMethod; // "pattern_based" or future "ml_based"

    public TypeDetectionResult(List<DetectedColumn> columns, String detectionMethod) {
        this.columns = columns;
        this.detectionMethod = detectionMethod;
    }

    public TypeDetectionResult(List<DetectedColumn> columns, Map<String, String> statistics, 
                              String detectionMethod) {
        this.columns = columns;
        this.statistics = statistics;
        this.detectionMethod = detectionMethod;
    }

    // Getters & Setters
    public List<DetectedColumn> getColumns() { return columns; }
    public void setColumns(List<DetectedColumn> columns) { this.columns = columns; }

    public Map<String, String> getStatistics() { return statistics; }
    public void setStatistics(Map<String, String> statistics) { this.statistics = statistics; }

    public String getDetectionMethod() { return detectionMethod; }
    public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }
}
