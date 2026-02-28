package com.movkfact.service.generator;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import com.movkfact.service.generator.financial.AccountNumberGenerator;
import com.movkfact.service.generator.financial.AmountGenerator;
import com.movkfact.service.generator.financial.CurrencyGenerator;
import com.movkfact.service.generator.personal.*;
import com.movkfact.service.generator.temporal.*;

/**
 * Factory pour créer les générateurs appropriés selon le type de colonne.
 * Utilise le pattern Strategy pour instancier le bon générateur.
 */
public class GeneratorFactory {
    
    /**
     * Crée un générateur approprié selon le type de colonne spécifié.
     * 
     * @param columnConfig Configuration de la colonne incluant type et paramètres
     * @return DataTypeGenerator instance du générateur approprié
     * @throws IllegalArgumentException si le type de colonne n'est pas supporté
     */
    public static DataTypeGenerator createGenerator(ColumnConfigDTO columnConfig) {
        if (columnConfig == null || columnConfig.getColumnType() == null) {
            throw new IllegalArgumentException("ColumnConfigDTO and ColumnType must not be null");
        }
        
        ColumnType columnType = columnConfig.getColumnType();
        
        // Personal typology
        switch (columnType) {
            case FIRST_NAME:
                return new FirstNameGenerator(columnConfig);
            case LAST_NAME:
                return new LastNameGenerator(columnConfig);
            case EMAIL:
                return new EmailGenerator(columnConfig);
            case GENDER:
                return new GenderGenerator(columnConfig);
            case PHONE:
                return new PhoneGenerator(columnConfig);
            case ADDRESS:
                return new AddressGenerator(columnConfig);
            
            // Financial typology
            case AMOUNT:
                return new AmountGenerator(columnConfig);
            case CURRENCY:
                return new CurrencyGenerator(columnConfig);
            case ACCOUNT_NUMBER:
                return new AccountNumberGenerator(columnConfig);
            
            // Temporal typology
            case DATE:
                return new DateGenerator(columnConfig);
            case TIME:
                return new TimeGenerator(columnConfig);
            case TIMEZONE:
                return new TimezoneGenerator(columnConfig);
            case BIRTH_DATE:
                return new BirthDateGenerator(columnConfig);
            
            default:
                throw new IllegalArgumentException("Unsupported column type: " + columnType);
        }
    }
}
