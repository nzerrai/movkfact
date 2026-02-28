package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Random;

/**
 * Générateur de numéros de téléphone français.
 * Format: +33 6 XX XX XX XX ou +33 1 XX XX XX XX
 */
public class PhoneGenerator extends DataTypeGenerator {
    private static final String[] PHONE_PREFIXES = {
        "+33 6 ",  // Mobile
        "+33 1 ",  // Île-de-France
        "+33 2 ",  // Ouest
        "+33 3 ",  // Est
        "+33 4 ",  // Sud-Est
        "+33 5 "   // Sud-Ouest
    };

    private static final Random random = new Random();

    public PhoneGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère un numéro de téléphone français aléatoire.
     * Format: 06XXXXXXXX ou 07XXXXXXXX (mobiles)
     * @return String - Numéro généré (ex: "0712345678")
     */
    @Override
    public Object generate() {
        String prefix = PHONE_PREFIXES[random.nextInt(PHONE_PREFIXES.length)];
        StringBuilder phone = new StringBuilder(prefix);
        
        for (int i = 0; i < 8; i++) {
            if (i > 0 && i % 2 == 0) {
                phone.append(" ");
            }
            phone.append(random.nextInt(10));
        }
        
        return phone.toString();
    }
}
