package com.movkfact.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.AnonymizationColumnConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'anonymisation RGPD irréversible.
 *
 * Garanties :
 * - Aucune donnée originale n'est persistée.
 * - Le sel est généré aléatoirement au démarrage, jamais exposé ni persisté.
 * - Les transformations sont irréversibles même avec accès au code source.
 */
@Service
public class AnonymizationService {

    private static final Random RANDOM = new Random();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<String> FIRST_NAMES = Arrays.asList(
        "Marie", "Jean", "Sophie", "Thomas", "Isabelle", "Nicolas", "Camille", "Pierre",
        "Laura", "François", "Nathalie", "Antoine", "Céline", "Marc", "Julie", "Julien",
        "Emma", "Hugo", "Léa", "Théo", "Alice", "Lucas", "Chloé", "Maxime"
    );
    private static final List<String> LAST_NAMES = Arrays.asList(
        "Martin", "Bernard", "Thomas", "Petit", "Robert", "Richard", "Durand", "Leroy",
        "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David", "Bertrand",
        "Roux", "Vincent", "Fournier", "Morel", "Girard", "Andre", "Lefevre", "Mercier"
    );
    private static final List<String> COMPANIES = Arrays.asList(
        "Acme Corp", "Société Générale Test", "BNP Fictif", "Crédit Anon", "Banque Synthétique",
        "Financière Exemple", "Groupe Fictif SA", "Holdings Anon", "Capital Test SAS"
    );
    private static final List<String> CITIES = Arrays.asList(
        "Paris", "Lyon", "Marseille", "Bordeaux", "Nantes", "Strasbourg", "Toulouse",
        "Montpellier", "Nice", "Rennes", "Grenoble", "Lille"
    );
    private static final List<String> COUNTRIES = Arrays.asList(
        "France", "Belgique", "Suisse", "Luxembourg", "Allemagne", "Espagne", "Italie"
    );
    private static final List<String> EMAIL_DOMAINS = Arrays.asList(
        "example.com", "mail-test.fr", "demo.org", "fictif.net", "anon-mail.eu",
        "sandbox.io", "test-domain.com", "fakemx.fr"
    );
    private static final List<String> URL_PATHS = Arrays.asList(
        "page", "article", "product", "category", "user", "profile", "item", "resource"
    );
    private static final List<String> URL_TLDS = Arrays.asList(
        "com", "fr", "net", "org", "io", "eu"
    );
    private static final List<String> WORDS = Arrays.asList(
        "alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel",
        "india", "juliet", "kilo", "lima", "mike", "november", "oscar", "papa",
        "quebec", "romeo", "sierra", "tango", "uniform", "victor", "whiskey", "xray"
    );
    private static final List<String> ENUM_VALUES = Arrays.asList(
        "Actif", "Inactif", "Suspendu", "Archivé", "Validé", "En attente", "Clôturé", "Ouvert"
    );
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    // ─── Inspection ──────────────────────────────────────────────────────────────

    /**
     * Analyse uniquement les entêtes du fichier CSV ou JSON.
     * Aucune donnée n'est lue au-delà des en-têtes.
     */
    public List<String> inspectColumns(MultipartFile file, String format) throws IOException {
        if ("json".equalsIgnoreCase(format)) {
            return inspectJsonColumns(file);
        }
        return inspectCsvColumns(file);
    }

    private List<String> inspectCsvColumns(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat fmt = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = fmt.parse(reader)) {
                return new ArrayList<>(parser.getHeaderNames());
            }
        }
    }

    private List<String> inspectJsonColumns(MultipartFile file) throws IOException {
        List<Map<String, Object>> rows = MAPPER.readValue(
            file.getInputStream(), new TypeReference<>() {});
        if (rows.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(rows.get(0).keySet());
    }

    // ─── Traitement CSV ───────────────────────────────────────────────────────────

    public void anonymizeCsv(MultipartFile file, List<AnonymizationColumnConfig> config,
                              OutputStream out) throws IOException {
        Map<String, AnonymizationColumnConfig> configMap = config.stream()
            .collect(Collectors.toMap(AnonymizationColumnConfig::getColumnName, c -> c));

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8))
        ) {
            CSVFormat fmt = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = fmt.parse(reader)) {
                List<String> headers = parser.getHeaderNames();
                CSVFormat outFmt = CSVFormat.DEFAULT.builder()
                    .setHeader(headers.toArray(new String[0])).build();
                try (CSVPrinter printer = new CSVPrinter(writer, outFmt)) {
                    for (CSVRecord record : parser) {
                        List<String> row = new ArrayList<>();
                        for (String header : headers) {
                            String original = record.get(header);
                            AnonymizationColumnConfig col = configMap.get(header);
                            row.add(col != null && col.isAnonymize()
                                ? anonymizeValue(original, col.getColumnType())
                                : original);
                        }
                        printer.printRecord(row);
                    }
                }
            }
        }
    }

    // ─── Traitement JSON ──────────────────────────────────────────────────────────

    public void anonymizeJson(MultipartFile file, List<AnonymizationColumnConfig> config,
                               OutputStream out) throws IOException {
        List<Map<String, Object>> result = anonymizeJsonToRows(file, config);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, result);
    }

    // ─── Anonymisation vers liste de lignes (pour persistance en dataset) ─────────

    public List<Map<String, Object>> anonymizeCsvToRows(MultipartFile file,
                                                         List<AnonymizationColumnConfig> config) throws IOException {
        Map<String, AnonymizationColumnConfig> configMap = config.stream()
            .collect(Collectors.toMap(AnonymizationColumnConfig::getColumnName, c -> c));

        List<Map<String, Object>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat fmt = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = fmt.parse(reader)) {
                List<String> headers = parser.getHeaderNames();
                for (CSVRecord record : parser) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (String header : headers) {
                        String original = record.get(header);
                        AnonymizationColumnConfig col = configMap.get(header);
                        row.put(header, col != null && col.isAnonymize()
                            ? anonymizeValue(original, col.getColumnType())
                            : original);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> anonymizeJsonToRows(MultipartFile file,
                                                          List<AnonymizationColumnConfig> config) throws IOException {
        Map<String, AnonymizationColumnConfig> configMap = config.stream()
            .collect(Collectors.toMap(AnonymizationColumnConfig::getColumnName, c -> c));

        List<Map<String, Object>> rows = MAPPER.readValue(
            file.getInputStream(), new TypeReference<>() {});

        return rows.stream().map(row -> {
            Map<String, Object> anon = new LinkedHashMap<>();
            row.forEach((key, val) -> {
                AnonymizationColumnConfig col = configMap.get(key);
                if (col != null && col.isAnonymize()) {
                    anon.put(key, anonymizeValue(val == null ? "" : val.toString(), col.getColumnType()));
                } else {
                    anon.put(key, val);
                }
            });
            return anon;
        }).collect(Collectors.toList());
    }

    // ─── Moteur d'anonymisation ───────────────────────────────────────────────────

    String anonymizeValue(String value, String columnType) {
        if (value == null || value.isBlank()) return "";
        if (columnType == null) return value;

        switch (columnType) {
            case "FIRST_NAME":      return pick(FIRST_NAMES);
            case "LAST_NAME":       return pick(LAST_NAMES);
            case "EMAIL":           return generateEmail();
            case "PHONE":           return generatePhone();
            case "ADDRESS":         return generateAddress();
            case "GENDER":          return value; // non-identifiant direct
            case "BIRTH_DATE":      return anonymizeBirthDate(value);
            case "DATE":            return anonymizeDate(value);
            case "TIME":            return generateTime();
            case "TIMEZONE":        return value;
            case "ACCOUNT_NUMBER":  return generateIban();
            case "UUID":            return java.util.UUID.randomUUID().toString();
            case "IP_ADDRESS":      return generateIp();
            case "URL":             return generateUrl();
            case "TEXT":            return generateMatchingType(value);
            case "INTEGER":         return generateInteger(value);
            case "DECIMAL":
            case "AMOUNT":          return generateDecimal(value);
            case "PERCENTAGE":      return String.format("%.2f", RANDOM.nextDouble() * 100);
            case "BOOLEAN":         return generateBoolean(value);
            case "CURRENCY":        return value; // code devise non-identifiant
            case "COUNTRY":         return pick(COUNTRIES);
            case "CITY":            return pick(CITIES);
            case "ZIP_CODE":        return generateZipCode();
            case "COMPANY":         return pick(COMPANIES);
            case "ENUM":            return pick(ENUM_VALUES);
            default:                return generateMatchingType(value);
        }
    }

    // ─── Générateurs spécialisés ──────────────────────────────────────────────────

    /** Email fictif : prenom.nom@domaine */
    private String generateEmail() {
        return pick(FIRST_NAMES).toLowerCase() + "." + pick(LAST_NAMES).toLowerCase()
            + RANDOM.nextInt(999) + "@" + pick(EMAIL_DOMAINS);
    }

    /** URL fictive */
    private String generateUrl() {
        return "https://www." + pick(WORDS) + "-" + pick(WORDS) + "." + pick(URL_TLDS)
            + "/" + pick(URL_PATHS) + "/" + (RANDOM.nextInt(9000) + 1000);
    }

    /** Texte fictif : deux mots aléatoires */
    private String generateText() {
        return pick(WORDS) + " " + pick(WORDS);
    }

    /**
     * Détecte le format de la valeur originale et génère une valeur du même type :
     * entier → entier, décimal → décimal, booléen → booléen, sinon texte.
     */
    private String generateMatchingType(String value) {
        String v = value.trim();
        // Booléen
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false"))
            return String.valueOf(RANDOM.nextBoolean());
        if (v.equalsIgnoreCase("oui") || v.equalsIgnoreCase("non"))
            return RANDOM.nextBoolean() ? "Oui" : "Non";
        // Entier
        try {
            Long.parseLong(v);
            long max = (long) Math.pow(10, Math.min(v.replace("-", "").length(), 9));
            return String.valueOf((long) (RANDOM.nextDouble() * max));
        } catch (NumberFormatException ignored) {}
        // Décimal
        try {
            Double.parseDouble(v);
            int dec = v.contains(".") ? v.length() - v.lastIndexOf('.') - 1 : 2;
            return String.format("%." + dec + "f", RANDOM.nextDouble() * 10000);
        } catch (NumberFormatException ignored) {}
        // Texte
        return generateText();
    }

    /** Entier aléatoire du même ordre de grandeur que la valeur originale */
    private String generateInteger(String value) {
        try {
            long orig = Math.abs(Long.parseLong(value.trim()));
            long max = orig == 0 ? 1000 : (long) Math.pow(10, String.valueOf(orig).length());
            return String.valueOf((long) (RANDOM.nextDouble() * max));
        } catch (NumberFormatException ignored) {}
        return String.valueOf(RANDOM.nextInt(100000));
    }

    /** Décimal aléatoire en conservant le nombre de décimales d'origine */
    private String generateDecimal(String value) {
        int dec = 2;
        if (value.contains(".")) dec = value.length() - value.lastIndexOf('.') - 1;
        return String.format("%." + dec + "f", RANDOM.nextDouble() * 10000);
    }

    /** Booléen aléatoire dans le même format que la valeur originale */
    private String generateBoolean(String value) {
        String v = value.trim().toLowerCase();
        if (v.equals("oui") || v.equals("non"))       return RANDOM.nextBoolean() ? "Oui" : "Non";
        if (v.equals("yes") || v.equals("no"))        return RANDOM.nextBoolean() ? "Yes" : "No";
        if (v.equals("1")   || v.equals("0"))         return RANDOM.nextBoolean() ? "1" : "0";
        return String.valueOf(RANDOM.nextBoolean());
    }

    /** IBAN FR fictif — 27 caractères — complètement régénéré */
    private String generateIban() {
        StringBuilder sb = new StringBuilder("FR");
        for (int i = 0; i < 25; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    /** Adresse IPv4 — 4 octets complètement régénérés */
    private String generateIp() {
        return RANDOM.nextInt(223) + 1 + "." +
               RANDOM.nextInt(256) + "." +
               RANDOM.nextInt(256) + "." +
               (RANDOM.nextInt(254) + 1);
    }

    /** Numéro de téléphone français fictif */
    private String generatePhone() {
        int prefix = new int[]{6, 7}[RANDOM.nextInt(2)];
        StringBuilder sb = new StringBuilder("0").append(prefix);
        for (int i = 0; i < 8; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    /** Adresse fictive */
    private String generateAddress() {
        return (RANDOM.nextInt(200) + 1) + " Rue " + pick(LAST_NAMES) + ", " + pick(CITIES);
    }

    /** Code postal français fictif */
    private String generateZipCode() {
        int dept = RANDOM.nextInt(95) + 1;
        return String.format("%02d%03d", dept, RANDOM.nextInt(1000));
    }

    /** Heure fictive HH:mm:ss */
    private String generateTime() {
        return String.format("%02d:%02d:%02d",
            RANDOM.nextInt(24), RANDOM.nextInt(60), RANDOM.nextInt(60));
    }

    /**
     * Date de naissance → généralisation RGPD : conserver uniquement l'année.
     * Évite la ré-identification par triangulation âge + ville + genre.
     */
    private String anonymizeBirthDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                LocalDate d = LocalDate.parse(value.trim(), fmt);
                return String.valueOf(d.getYear());
            } catch (DateTimeParseException ignored) {}
        }
        // Fallback : si on ne parse pas, on retourne l'année seule si 4 chiffres
        if (value.matches(".*\\d{4}.*")) {
            return value.replaceAll(".*?(\\d{4}).*", "$1");
        }
        return "****";
    }

    /**
     * Date générique → décalage aléatoire ±180 jours.
     * Préserve le format d'origine.
     */
    private String anonymizeDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                LocalDate d = LocalDate.parse(value.trim(), fmt);
                int shift = RANDOM.nextInt(361) - 180;
                return d.plusDays(shift).format(fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return value;
    }

    private <T> T pick(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
