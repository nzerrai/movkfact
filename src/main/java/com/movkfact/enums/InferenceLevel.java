package com.movkfact.enums;

/**
 * Indicates which inference mechanism determined the column type.
 * LEARNED    : type from user-validated corpus (Niveau 0 — S10.1)
 * NAME_BASED : type inferred from column name (Niveau 1)
 * DATA_BASED : type inferred from sample data analysis (Niveau 2)
 */
public enum InferenceLevel {
    LEARNED,
    NAME_BASED,
    DATA_BASED
}
