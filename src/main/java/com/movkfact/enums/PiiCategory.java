package com.movkfact.enums;

/**
 * Category of personally identifiable information (PII) — S9.2.
 */
public enum PiiCategory {
    CONTACT,    // EMAIL, PHONE
    IDENTITY,   // FIRST_NAME, LAST_NAME, IBAN, NIR, SIRET
    LOCATION    // POSTAL_CODE, CITY
}
