package com.movkfact.dto;

import com.movkfact.enums.InferenceLevel;
import com.movkfact.enums.PiiCategory;

/**
 * Result of PiiDetectionService.detect() — S9.2.
 */
public class PiiResult {

    private final boolean pii;
    private final PiiCategory category;
    private final InferenceLevel detectedBy;

    public PiiResult(boolean pii, PiiCategory category, InferenceLevel detectedBy) {
        this.pii = pii;
        this.category = category;
        this.detectedBy = detectedBy;
    }

    public boolean isPii()                { return pii; }
    public PiiCategory getCategory()      { return category; }
    public InferenceLevel getDetectedBy() { return detectedBy; }
}
