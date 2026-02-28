package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.*;

/**
 * Générateur d'adresses multi-pays avec 3 formats et 3 pays.
 * Formats: full (adresse complète), street (rue+numéro), city (ville seule)
 * Pays: FR (défaut), US, DE. Configuration via additionalConfig JSON.
 */
public class AddressGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    // France
    private static final String[] FR_STREET_TYPES = {"Rue", "Avenue", "Boulevard", "Chemin", "Place", "Allée"};
    private static final String[] FR_STREET_NAMES = {
        "de la Paix", "Voltaire", "de la République", "Jean Jaurès", "de Rivoli",
        "de Turenne", "Colbert", "Montmartre", "Barbès", "de Belleville",
        "Sébastopol", "Strasbourg", "Saint-Honoré", "Castiglione", "Lafayette"
    };
    private static final String[] FR_CITIES = {
        "Paris", "Marseille", "Lyon", "Toulouse", "Nice",
        "Nantes", "Strasbourg", "Bordeaux", "Lille", "Rennes",
        "Reims", "Le Havre", "Saint-Étienne", "Toulon", "Grenoble"
    };
    private static final String[] FR_POSTAL_CODES = {
        "75001", "75002", "13001", "69001", "31000",
        "06000", "44000", "67000", "33000", "59000",
        "51100", "76600", "42000", "83000", "38000"
    };

    // USA
    private static final String[] US_STREETS = {
        "Main St", "Oak St", "Maple St", "Elm St", "Oak Ave",
        "First St", "Second St", "Park Ave", "Broadway", "Fifth Ave"
    };
    private static final String[] US_CITIES = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"
    };
    private static final String[] US_STATES = {
        "NY", "CA", "TX", "FL", "PA", "IL", "OH", "GA", "NC", "MI"
    };

    // Germany
    private static final String[] DE_STREETS = {
        "Hauptstraße", "Marktstraße", "Schulstraße", "Kirchenstraße", "Bahnstraße",
        "Berliner Straße", "Waldstraße", "Gartenstraße", "Blütenstraße", "Rosenstraße"
    };
    private static final String[] DE_CITIES = {
        "Berlin", "Munich", "Cologne", "Frankfurt", "Hamburg",
        "Düsseldorf", "Dortmund", "Essen", "Stuttgart", "Leipzig"
    };

    public AddressGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère une adresse aléatoire selon format et pays configurés.
     * Format complet: "42 Rue de la Paix, 75001 Paris"
     * @return String - Adresse générée (ex: "123 Main St, New York, NY 10001")
     */
    @Override
    public Object generate() {
        String country = getCountry();
        String format = getFormat();
        
        switch (format.toLowerCase()) {
            case "street":
                return generateStreet(country);
            case "city":
                return generateCity(country);
            default:
            case "full":
                return generateFullAddress(country);
        }
    }

    private String generateFullAddress(String country) {
        switch (country.toUpperCase()) {
            case "US":
                return generateFullUS();
            case "DE":
                return generateFullDE();
            case "FR":
            default:
                return generateFullFR();
        }
    }

    private String generateFullFR() {
        int streetNumber = 1 + random.nextInt(500);
        String streetType = FR_STREET_TYPES[random.nextInt(FR_STREET_TYPES.length)];
        String streetName = FR_STREET_NAMES[random.nextInt(FR_STREET_NAMES.length)];
        String postalCode = FR_POSTAL_CODES[random.nextInt(FR_POSTAL_CODES.length)];
        String city = FR_CITIES[random.nextInt(FR_CITIES.length)];
        
        return streetNumber + " " + streetType + " " + streetName + ", " + postalCode + " " + city;
    }

    private String generateFullUS() {
        int streetNumber = 1 + random.nextInt(9999);
        String street = US_STREETS[random.nextInt(US_STREETS.length)];
        String city = US_CITIES[random.nextInt(US_CITIES.length)];
        String state = US_STATES[random.nextInt(US_STATES.length)];
        int zipCode = 10000 + random.nextInt(90000);
        
        return streetNumber + " " + street + ", " + city + ", " + state + " " + zipCode;
    }

    private String generateFullDE() {
        int streetNumber = 1 + random.nextInt(500);
        String street = DE_STREETS[random.nextInt(DE_STREETS.length)];
        String city = DE_CITIES[random.nextInt(DE_CITIES.length)];
        int postalCode = 10000 + random.nextInt(90000);
        
        return streetNumber + " " + street + ", " + postalCode + " " + city;
    }

    private String generateStreet(String country) {
        switch (country.toUpperCase()) {
            case "US":
                int usStreetNumber = 1 + random.nextInt(9999);
                return usStreetNumber + " " + US_STREETS[random.nextInt(US_STREETS.length)];
            case "DE":
                int deStreetNumber = 1 + random.nextInt(500);
                return deStreetNumber + " " + DE_STREETS[random.nextInt(DE_STREETS.length)];
            case "FR":
            default:
                int streetNumber = 1 + random.nextInt(500);
                String streetType = FR_STREET_TYPES[random.nextInt(FR_STREET_TYPES.length)];
                String streetName = FR_STREET_NAMES[random.nextInt(FR_STREET_NAMES.length)];
                return streetNumber + " " + streetType + " " + streetName;
        }
    }

    private String generateCity(String country) {
        switch (country.toUpperCase()) {
            case "US":
                return US_CITIES[random.nextInt(US_CITIES.length)];
            case "DE":
                return DE_CITIES[random.nextInt(DE_CITIES.length)];
            case "FR":
            default:
                return FR_CITIES[random.nextInt(FR_CITIES.length)];
        }
    }

    /**
     * Extrait le code pays depuis la configuration JSON.
     * Cherche le champ "country" avec valeurs supportées: FR (défaut), US, DE.
     * En cas d'erreur de parsing, retourne "FR" par défaut.
     * 
     * @return Code ISO pays (2 caractères) ou "FR" par défaut
     */
    private String getCountry() {
        if (columnConfig.getAdditionalConfig() != null && !columnConfig.getAdditionalConfig().isEmpty()) {
            try {
                // Utiliser Jackson pour parsing JSON robuste (disponible via spring-boot-starter-web)
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(columnConfig.getAdditionalConfig());
                
                if (node.has("country")) {
                    String countryCode = node.get("country").asText().toUpperCase();
                    // Valider que c'est un code pays supporté (FR, US, DE)
                    if (countryCode.matches("^(FR|US|DE)$")) {
                        return countryCode;
                    } else {
                        System.err.println("Warning: Country code '" + countryCode + "' not supported (FR/US/DE), using default FR");
                    }
                }
            } catch (Exception e) {
                // Si le JSON ne peut pas être parsé, log warning mais ne fail pas
                System.err.println("Warning: Failed to parse additionalConfig JSON for AddressGenerator: " + e.getMessage());
            }
        }
        return "FR";  // Default to France
    }

    private String getFormat() {
        if (columnConfig.getFormat() != null) {
            return columnConfig.getFormat();
        }
        return "full";  // Default
    }
}
