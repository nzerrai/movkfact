package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AddressGenerator.
 */
class AddressGeneratorTests {

    @Test
    void testAddressGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("full");
        AddressGenerator generator = new AddressGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleFullAddress() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("full");
        AddressGenerator generator = new AddressGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String address = (String) result;
        assertTrue(address.length() > 5);
    }

    @Test
    void testGenerateStreetOnlyFormat() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("street");
        AddressGenerator generator = new AddressGenerator(config);
        
        String address = (String) generator.generate();
        // Street format should contain number and street type
        assertTrue(address.length() > 0);
        // Should not contain postal code or comma
        assertFalse(address.contains(","), "Street format should not contain commas");
    }

    @Test
    void testGenerateCityOnlyFormat() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("city");
        AddressGenerator generator = new AddressGenerator(config);
        
        String address = (String) generator.generate();
        // Should be just a city name
        assertFalse(address.contains(","), "City format should not contain commas");
        assertFalse(address.matches(".*\\d+.*"), "City should not contain numbers");
    }

    @Test
    void testGenerateMultipleAddresses() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("full");
        AddressGenerator generator = new AddressGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(50);
        assertEquals(50, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }

    @Test
    void testFrenchAddressGeneration() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("full");
        config.setAdditionalConfig("{\"country\": \"FR\"}");
        AddressGenerator generator = new AddressGenerator(config);
        
        for (int i = 0; i < 20; i++) {
            String address = (String) generator.generate();
            // French addresses should contain common patterns
            assertTrue(address.length() > 10);
        }
    }

    @Test
    void testAddressVariety() {
        ColumnConfigDTO config = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        config.setFormat("city");
        AddressGenerator generator = new AddressGenerator(config);
        
        java.util.Set<String> uniqueAddresses = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            uniqueAddresses.add((String) generator.generate());
        }
        
        // Should generate variety of unique addresses
        assertTrue(uniqueAddresses.size() > 5);
    }
}
