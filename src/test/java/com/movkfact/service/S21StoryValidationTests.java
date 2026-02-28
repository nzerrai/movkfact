package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests validating S2.1 story acceptance criteria.
 * Tests real-world generation scenarios to ensure the service works as expected.
 */
@SpringBootTest
class S21StoryValidationTests {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    // ============================================================================
    // AC1: DataGeneratorService created & available
    // ============================================================================
    @Test
    void testAC1_DataGeneratorServiceExists() {
        assertNotNull(dataGeneratorService, "DataGeneratorService must be injected");
    }

    // ============================================================================
    // AC2: 3 typologies implemented with all generators
    // ============================================================================
    @Test
    void testAC2_PersonalGeneratorsWorking() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("lastname", ColumnType.LAST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("gender", ColumnType.GENDER));
        columns.add(new ColumnConfigDTO("phone", ColumnType.PHONE));

        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        assertNotNull(response);
        assertEquals(10, response.getNumberOfRows());
        assertEquals(6, response.getData().get(0).size()); // All 6 columns populated

        // Validate each column is actually populated with data
        Map<String, Object> firstRow = response.getData().get(0);
        assertNotNull(firstRow.get("firstname"), "firstname must be generated");
        assertNotNull(firstRow.get("lastname"), "lastname must be generated");
        assertNotNull(firstRow.get("email"), "email must be generated");
        assertNotNull(firstRow.get("gender"), "gender must be generated");
        assertNotNull(firstRow.get("phone"), "phone must be generated");
        assertNotNull(firstRow.get("address"), "address must be generated");
    }

    @Test
    void testAC2_FinancialGeneratorsWorking() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("amount", ColumnType.AMOUNT));
        columns.add(new ColumnConfigDTO("currency", ColumnType.CURRENCY));
        columns.add(new ColumnConfigDTO("account", ColumnType.ACCOUNT_NUMBER));

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        assertEquals(10, response.getNumberOfRows());
        Map<String, Object> firstRow = response.getData().get(0);

        assertNotNull(firstRow.get("amount"), "amount must be generated");
        assertNotNull(firstRow.get("currency"), "currency must be generated");
        assertNotNull(firstRow.get("account"), "account must be generated");

        // Validate format
        Object amountObj = firstRow.get("amount");
        String amount = amountObj instanceof String ? (String) amountObj : amountObj.toString();
        assertTrue(Pattern.matches("\\d+\\.\\d{2}", amount), "Amount must be XX.XX format");

        String currency = (String) firstRow.get("currency");
        assertEquals(3, currency.length(), "Currency must be 3-letter ISO code");

        String account = (String) firstRow.get("account");
        assertTrue(account.startsWith("****"), "Account must be masked as ****XXXX");
    }

    @Test
    void testAC2_TemporalGeneratorsWorking() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("date", ColumnType.DATE));
        columns.add(new ColumnConfigDTO("time", ColumnType.TIME));
        columns.add(new ColumnConfigDTO("timezone", ColumnType.TIMEZONE));

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        assertEquals(10, response.getNumberOfRows());
        Map<String, Object> firstRow = response.getData().get(0);

        assertNotNull(firstRow.get("date"), "date must be generated");
        assertNotNull(firstRow.get("time"), "time must be generated");
        assertNotNull(firstRow.get("timezone"), "timezone must be generated");

        // Validate format
        String date = (String) firstRow.get("date");
        assertTrue(Pattern.matches("\\d{4}-\\d{2}-\\d{2}", date), "Date must be yyyy-MM-dd format");

        String time = (String) firstRow.get("time");
        assertTrue(Pattern.matches("\\d{2}:\\d{2}:\\d{2}", time), "Time must be HH:mm:ss format");
    }

    // ============================================================================
    // AC3: Configuration per column
    // ============================================================================
    @Test
    void testAC3_ColumnConfigurationWorking() {
        List<ColumnConfigDTO> columns = new ArrayList<>();

        // Test format configuration for address
        ColumnConfigDTO addressFull = new ColumnConfigDTO("address_full", ColumnType.ADDRESS);
        addressFull.setFormat("full");
        columns.add(addressFull);

        ColumnConfigDTO addressStreet = new ColumnConfigDTO("address_street", ColumnType.ADDRESS);
        addressStreet.setFormat("street");
        columns.add(addressStreet);

        ColumnConfigDTO addressCity = new ColumnConfigDTO("address_city", ColumnType.ADDRESS);
        addressCity.setFormat("city");
        columns.add(addressCity);

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        Map<String, Object> firstRow = response.getData().get(0);

        // Full address should contain comma (number + street + postal + city)
        String full = (String) firstRow.get("address_full");
        assertTrue(full.contains(","), "Full address must contain comma separator");

        // Street should not contain comma
        String street = (String) firstRow.get("address_street");
        assertFalse(street.contains(","), "Street format should not contain comma");

        // City should be just a city name, no numbers or commas
        String city = (String) firstRow.get("address_city");
        assertFalse(city.contains(","), "City format should not contain comma");
    }

    // ============================================================================
    // AC4: JSON-configurable generation with row count
    // ============================================================================
    @Test
    void testAC4_JSONConfigurableGeneration() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("first_name", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));

        GenerationRequestDTO request = new GenerationRequestDTO(50, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        assertEquals(50, response.getNumberOfRows(), "Must generate exactly requested rows");
        assertEquals(2, response.getData().get(0).size(), "Must have exactly 2 columns");

        // Validate each row has data
        for (int i = 0; i < 50; i++) {
            Map<String, Object> row = response.getData().get(i);
            assertNotNull(row.get("first_name"), "Row " + i + " must have first_name");
            assertNotNull(row.get("email"), "Row " + i + " must have email");
        }
    }

    // ============================================================================
    // AC5: Performance - 1000 rows < 2 seconds
    // ============================================================================
    @Test
    void testAC5_Performance1000Rows() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("lastname", ColumnType.LAST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("phone", ColumnType.PHONE));

        GenerationRequestDTO request = new GenerationRequestDTO(1000, columns);

        long startTime = System.currentTimeMillis();
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertEquals(1000, response.getNumberOfRows());
        assertTrue(elapsedTime < 2000, "1000 rows must be generated in < 2 seconds, took " + elapsedTime + "ms");
        System.out.println("✅ Performance: 1000 rows generated in " + elapsedTime + "ms");
    }

    // ============================================================================
    // AC8-A: Birth Date - ADULT_LIVING (18-99 years old)
    // ============================================================================
    @Test
    void testAC8A_BirthDateAdultLiving() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO birthDate = new ColumnConfigDTO("dob", ColumnType.BIRTH_DATE);
        birthDate.setAdditionalConfig("{\"ageCategory\": \"ADULT_LIVING\"}");
        columns.add(birthDate);

        GenerationRequestDTO request = new GenerationRequestDTO(50, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            String dobStr = (String) response.getData().get(i).get("dob");
            LocalDate dob = LocalDate.parse(dobStr);

            long ageYears = ChronoUnit.YEARS.between(dob, today);
            assertTrue(ageYears >= 18 && ageYears <= 99,
                    "Row " + i + ": ADULT_LIVING age must be 18-99, got " + ageYears);
        }
    }

    // ============================================================================
    // AC8-B: Birth Date - MINOR_LIVING (0-17 years old)
    // ============================================================================
    @Test
    void testAC8B_BirthDateMinorLiving() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO birthDate = new ColumnConfigDTO("dob", ColumnType.BIRTH_DATE);
        birthDate.setAdditionalConfig("{\"ageCategory\": \"MINOR_LIVING\"}");
        columns.add(birthDate);

        GenerationRequestDTO request = new GenerationRequestDTO(50, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            String dobStr = (String) response.getData().get(i).get("dob");
            LocalDate dob = LocalDate.parse(dobStr);

            long ageYears = ChronoUnit.YEARS.between(dob, today);
            assertTrue(ageYears >= 0 && ageYears <= 17,
                    "Row " + i + ": MINOR_LIVING age must be 0-17, got " + ageYears);
        }
    }

    // ============================================================================
    // AC8-C: Birth Date - DECEASED (50-150 years, max 50 years ago)
    // ============================================================================
    @Test
    void testAC8C_BirthDateDeceased() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO birthDate = new ColumnConfigDTO("dob", ColumnType.BIRTH_DATE);
        birthDate.setAdditionalConfig("{\"ageCategory\": \"DECEASED\"}");
        columns.add(birthDate);

        GenerationRequestDTO request = new GenerationRequestDTO(50, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            String dobStr = (String) response.getData().get(i).get("dob");
            LocalDate dob = LocalDate.parse(dobStr);

            long ageYears = ChronoUnit.YEARS.between(dob, today);
            // DECEASED: 50-150 years old
            assertTrue(ageYears >= 50 && ageYears <= 150,
                    "Row " + i + ": DECEASED age must be 50-150, got " + ageYears);
        }
    }

    // ============================================================================
    // AC9-A: Address Formats - FULL (numéro + rue + code postal + ville)
    // ============================================================================
    @Test
    void testAC9A_AddressFormatFull() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        address.setAdditionalConfig("{\"country\": \"FR\"}");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(20, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        for (int i = 0; i < 20; i++) {
            String addr = (String) response.getData().get(i).get("address");
            // Full FR address should contain: number + street type + street name + postal + city
            assertTrue(addr.contains(","), "Full address must contain comma");
            assertTrue(addr.matches(".*\\d+.*"), "Full address must contain street number");
        }
    }

    // ============================================================================
    // AC9-B: Address Formats - STREET (rue et numéro seulement)
    // ============================================================================
    @Test
    void testAC9B_AddressFormatStreet() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("street");
        address.setAdditionalConfig("{\"country\": \"FR\"}");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(20, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        for (int i = 0; i < 20; i++) {
            String addr = (String) response.getData().get(i).get("address");
            // Street format should not contain comma (no postal/city)
            assertFalse(addr.contains(","), "Street format should not contain comma");
            assertTrue(addr.matches(".*\\d+.*"), "Street must contain street number");
        }
    }

    // ============================================================================
    // AC9-C: Address Formats - CITY (ville seulement)
    // ============================================================================
    @Test
    void testAC9C_AddressFormatCity() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("city");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(20, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        for (int i = 0; i < 20; i++) {
            String addr = (String) response.getData().get(i).get("address");
            // City format should be just text, no numbers or commas
            assertFalse(addr.contains(","), "City format should not contain comma");
            // City names should be text-only (no numbers)
            assertTrue(addr.length() > 0, "City must not be empty");
        }
    }

    // ============================================================================
    // AC9-D: Address Multi-country (FR, US, DE)
    // ============================================================================
    @Test
    void testAC9D_AddressMultiCountryUS() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        address.setAdditionalConfig("{\"country\": \"US\"}");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        for (int i = 0; i < 10; i++) {
            String addr = (String) response.getData().get(i).get("address");
            // US addresses should be generated
            assertTrue(addr.length() > 5, "US address must be generated");
        }
    }

    @Test
    void testAC9D_AddressMultiCountryDE() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        address.setAdditionalConfig("{\"country\": \"DE\"}");
        columns.add(address);

        GenerationRequestDTO request = new GenerationRequestDTO(10, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        for (int i = 0; i < 10; i++) {
            String addr = (String) response.getData().get(i).get("address");
            // DE addresses should be generated
            assertTrue(addr.length() > 5, "DE address must be generated");
        }
    }

    // ============================================================================
    // AC10: No exceptions for standard cases
    // ============================================================================
    @Test
    void testAC10_NoExceptionsForStandardCases() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("name", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("phone", ColumnType.PHONE));
        columns.add(new ColumnConfigDTO("amount", ColumnType.AMOUNT));

        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            GenerationResponseDTO response = dataGeneratorService.generate(request);
            assertNotNull(response);
            assertEquals(100, response.getNumberOfRows());
        });
    }

    // ============================================================================
    // AC11: Data Variety - ensure random generation produces variety
    // ============================================================================
    @Test
    void testAC11_DataVariety() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));

        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        Set<Object> uniqueNames = new java.util.HashSet<>();
        for (Map<String, Object> row : response.getData()) {
            uniqueNames.add(row.get("firstname"));
        }

        // Should have variety (not all same value)
        assertTrue(uniqueNames.size() > 10, "Should generate variety of names, got " + uniqueNames.size());
    }

    // ============================================================================
    // Integration Test: Real-world scenario
    // ============================================================================
    @Test
    void testRealWorldScenario_CustomerDataGeneration() {
        /**
         * Real-world scenario: Generate customer data for testing
         * - 100 customers
         * - Full name, email, phone, address
         * - Birth date (adults only)
         * - Account information
         */
        List<ColumnConfigDTO> columns = new ArrayList<>();

        columns.add(new ColumnConfigDTO("first_name", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("last_name", ColumnType.LAST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("phone", ColumnType.PHONE));

        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        columns.add(address);

        ColumnConfigDTO dob = new ColumnConfigDTO("date_of_birth", ColumnType.BIRTH_DATE);
        dob.setAdditionalConfig("{\"ageCategory\": \"ADULT_LIVING\"}");
        columns.add(dob);

        columns.add(new ColumnConfigDTO("account_number", ColumnType.ACCOUNT_NUMBER));
        columns.add(new ColumnConfigDTO("currency", ColumnType.CURRENCY));

        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        // Validate structure
        assertEquals(100, response.getNumberOfRows());
        assertEquals(8, response.getData().get(0).size());

        // Validate first customer record
        Map<String, Object> customer = response.getData().get(0);
        assertNotNull(customer.get("first_name"));
        assertNotNull(customer.get("last_name"));
        assertNotNull(customer.get("email"));
        assertNotNull(customer.get("phone"));
        assertNotNull(customer.get("address"));
        assertNotNull(customer.get("date_of_birth"));
        assertNotNull(customer.get("account_number"));
        assertNotNull(customer.get("currency"));

        System.out.println("✅ Real-world scenario: Generated 100 customer records successfully");
    }
}
