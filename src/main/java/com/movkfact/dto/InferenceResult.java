package com.movkfact.dto;

import com.movkfact.enums.ColumnType;
import com.movkfact.enums.InferenceLevel;

/**
 * Result of ColumnTypeInferenceService.infer() — type + confidence + level.
 * confidence: 0–100 scale. HIGH ≥ 85, MEDIUM 60–84, LOW < 60.
 */
public class InferenceResult {

    private ColumnType type;
    private double confidence;
    private InferenceLevel level;
    /** S10.1 — nombre de validations ayant conduit à ce mapping (0 si non-LEARNED). */
    private int learnedCount;

    public InferenceResult(ColumnType type, double confidence, InferenceLevel level) {
        this.type = type;
        this.confidence = confidence;
        this.level = level;
    }

    public InferenceResult(ColumnType type, double confidence, InferenceLevel level, int learnedCount) {
        this(type, confidence, level);
        this.learnedCount = learnedCount;
    }

    public ColumnType getType()       { return type; }
    public double getConfidence()     { return confidence; }
    public InferenceLevel getLevel()  { return level; }
    public int getLearnedCount()      { return learnedCount; }
}
