package com.movkfact.service.generator.financial;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Random;

/**
 * Générateur de numéros de compte bancaire masqués au format ****XXXX.
 * Format de sécurité courant affichant seuls les 4 derniers chiffres.
 */
public class AccountNumberGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public AccountNumberGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère un numéro de compte masqué au format ****XXXX.
     * Seuls les 4 derniers chiffres sont visibles pour des raisons de sécurité.
     *
     * @return String - Numéro de compte masqué (ex: "****5678")
     */
    @Override
    public Object generate() {
        // Generate a 16-digit account number, but mask all but last 4 digits
        StringBuilder sb = new StringBuilder("****");
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
