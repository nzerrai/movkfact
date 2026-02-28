package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur d'adresses email aléatoires.
 * Génère des emails au format {username}@{domain} où:
 * - username: 8 caractères alphanumériques aléatoires (a-z, 0-9)
 * - domain: sélectionné aléatoirement parmi 12 domaines FR et internationaux
 *
 * Domaines supportés: gmail.com, yahoo.fr, outlook.com, hotmail.fr, orange.fr, sfr.fr, etc.
 */
public class EmailGenerator extends DataTypeGenerator {
    /** Domaines email disponibles incluant fournisseurs FR et internationaux */
    private static final List<String> DOMAINS = Arrays.asList(
        "gmail.com", "yahoo.fr", "outlook.com", "hotmail.fr",
        "orange.fr", "sfr.fr", "free.fr", "laposte.net",
        "wanadoo.fr", "numericable.fr", "bbox.fr", "club-internet.fr"
    );

    private static final Random random = new Random();

    /**
     * Constructeur initialisant le générateur avec configuration de colonne.
     * @param columnConfig Configuration de la colonne email (format, validation)
     */
    public EmailGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère une adresse email aléatoire au format valide.
     * Format: {username}@{domain}
     * - username: 8 caractères alphanumériques (a-z, 0-9)
     * - domain: sélectionné aléatoirement depuis DOMAINS
     *
     * @return String - Adresse email générée (ex: "abc123xyz@gmail.com")
     */
    @Override
    public Object generate() {
        String username = generateUsername(8);
        String domain = DOMAINS.get(random.nextInt(DOMAINS.size()));
        return username + "@" + domain;
    }

    /**
     * Génère un nom d'utilisateur aléatoire de longueur spécifiée.
     * Utilise uniquement des caractères alphanumériques minuscules (a-z, 0-9).
     *
     * @param length Longueur du nom d'utilisateur à générer (ex: 8)
     * @return String - Nom d'utilisateur généré (ex: "xyz123ab")
     */
    private String generateUsername(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
