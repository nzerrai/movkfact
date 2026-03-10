package com.movkfact.dto;

/**
 * Représente le feedback d'une colonne envoyé par le frontend après génération (S10.1).
 *
 * <p>Envoyé dans un tableau : {@code POST /api/csv/feedback}.</p>
 * <ul>
 *   <li>{@code colName} — nom de la colonne tel qu'il apparaît dans le CSV</li>
 *   <li>{@code detectedType} — type détecté automatiquement (peut être null)</li>
 *   <li>{@code validatedType} — type choisi par l'utilisateur</li>
 * </ul>
 */
public class ColumnFeedbackRequest {

    private String colName;
    private String detectedType;
    private String validatedType;

    public ColumnFeedbackRequest() {}

    public ColumnFeedbackRequest(String colName, String detectedType, String validatedType) {
        this.colName = colName;
        this.detectedType = detectedType;
        this.validatedType = validatedType;
    }

    public String getColName()        { return colName; }
    public String getDetectedType()   { return detectedType; }
    public String getValidatedType()  { return validatedType; }

    public void setColName(String colName)               { this.colName = colName; }
    public void setDetectedType(String detectedType)     { this.detectedType = detectedType; }
    public void setValidatedType(String validatedType)   { this.validatedType = validatedType; }
}
