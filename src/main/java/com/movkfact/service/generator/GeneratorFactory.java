package com.movkfact.service.generator;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import com.movkfact.service.generator.financial.AccountNumberGenerator;
import com.movkfact.service.generator.financial.AmountGenerator;
import com.movkfact.service.generator.financial.CurrencyGenerator;
import com.movkfact.service.generator.numeric.IntegerGenerator;
import com.movkfact.service.generator.numeric.DecimalGenerator;
import com.movkfact.service.generator.numeric.PercentageGenerator;
import com.movkfact.service.generator.numeric.BooleanGenerator;
import com.movkfact.service.generator.text.EnumGenerator;
import com.movkfact.service.generator.text.TextGenerator;
import com.movkfact.service.generator.text.UuidGenerator;
import com.movkfact.service.generator.text.UrlGenerator;
import com.movkfact.service.generator.text.IpAddressGenerator;
import com.movkfact.service.generator.geographic.CountryGenerator;
import com.movkfact.service.generator.geographic.CityGenerator;
import com.movkfact.service.generator.geographic.CompanyGenerator;
import com.movkfact.service.generator.geographic.ZipCodeGenerator;
import com.movkfact.service.generator.personal.*;
import com.movkfact.service.generator.temporal.*;

/**
 * Factory pour créer les générateurs appropriés selon le type de colonne.
 * Utilise le pattern Strategy pour instancier le bon générateur.
 */
public class GeneratorFactory {

    public static DataTypeGenerator createGenerator(ColumnConfigDTO columnConfig) {
        if (columnConfig == null || columnConfig.getColumnType() == null) {
            throw new IllegalArgumentException("ColumnConfigDTO and ColumnType must not be null");
        }

        ColumnType columnType = columnConfig.getColumnType();

        switch (columnType) {
            // Personal
            case FIRST_NAME:    return new FirstNameGenerator(columnConfig);
            case LAST_NAME:     return new LastNameGenerator(columnConfig);
            case EMAIL:         return new EmailGenerator(columnConfig);
            case GENDER:        return new GenderGenerator(columnConfig);
            case PHONE:         return new PhoneGenerator(columnConfig);
            case ADDRESS:       return new AddressGenerator(columnConfig);

            // Numeric
            case INTEGER:       return new IntegerGenerator(columnConfig);
            case DECIMAL:       return new DecimalGenerator(columnConfig);
            case PERCENTAGE:    return new PercentageGenerator(columnConfig);
            case BOOLEAN:       return new BooleanGenerator(columnConfig);

            // Text
            case ENUM:          return new EnumGenerator(columnConfig);
            case TEXT:          return new TextGenerator(columnConfig);
            case UUID:          return new UuidGenerator(columnConfig);
            case URL:           return new UrlGenerator(columnConfig);
            case IP_ADDRESS:    return new IpAddressGenerator(columnConfig);

            // Geographic
            case COUNTRY:       return new CountryGenerator(columnConfig);
            case CITY:          return new CityGenerator(columnConfig);
            case COMPANY:       return new CompanyGenerator(columnConfig);
            case ZIP_CODE:      return new ZipCodeGenerator(columnConfig);

            // Financial
            case AMOUNT:        return new AmountGenerator(columnConfig);
            case CURRENCY:      return new CurrencyGenerator(columnConfig);
            case ACCOUNT_NUMBER: return new AccountNumberGenerator(columnConfig);

            // Temporal
            case DATE:          return new DateGenerator(columnConfig);
            case TIME:          return new TimeGenerator(columnConfig);
            case TIMEZONE:      return new TimezoneGenerator(columnConfig);
            case BIRTH_DATE:    return new BirthDateGenerator(columnConfig);

            default:
                throw new IllegalArgumentException("Unsupported column type: " + columnType);
        }
    }
}
