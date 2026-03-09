package com.movkfact.enums;

/**
 * Indicates which inference mechanism determined the column type.
 * NAME_BASED : type inferred from column name (Niveau 1)
 * DATA_BASED  : type inferred from sample data analysis (Niveau 2)
 */
public enum InferenceLevel {
    NAME_BASED,
    DATA_BASED
}
