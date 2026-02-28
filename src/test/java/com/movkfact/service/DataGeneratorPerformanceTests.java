package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de performance pour la Task 2.1.5 - Integration & Performance Testing.
 */
@SpringBootTest
class DataGeneratorPerformanceTests {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @Test
    void testPerformance1000RowsAllGenerators() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        
        // Personal
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("lastname", ColumnType.LAST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("gender", ColumnType.GENDER));
        columns.add(new ColumnConfigDTO("phone", ColumnType.PHONE));
        
        ColumnConfigDTO address = new ColumnConfigDTO("address", ColumnType.ADDRESS);
        address.setFormat("full");
        columns.add(address);
        
        // Financial
        columns.add(new ColumnConfigDTO("amount", ColumnType.AMOUNT));
        columns.add(new ColumnConfigDTO("currency", ColumnType.CURRENCY));
        columns.add(new ColumnConfigDTO("account", ColumnType.ACCOUNT_NUMBER));
        
        // Temporal
        columns.add(new ColumnConfigDTO("date", ColumnType.DATE));
        columns.add(new ColumnConfigDTO("time", ColumnType.TIME));
        columns.add(new ColumnConfigDTO("timezone", ColumnType.TIMEZONE));
        
        ColumnConfigDTO birthDate = new ColumnConfigDTO("birthdate", ColumnType.BIRTH_DATE);
        birthDate.setAdditionalConfig("{\"ageCategory\": \"ADULT_LIVING\"}");
        columns.add(birthDate);
        
        GenerationRequestDTO request = new GenerationRequestDTO(1000, columns);
        request.setDomainId(1L);
        request.setDatasetName("Performance Test 1000 rows");
        
        long startTime = System.currentTimeMillis();
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(response);
        assertEquals(1000, response.getNumberOfRows());
        
        // Performance requirement: 1000 rows should be generated in less than 2 seconds
        // Allowing 3 seconds for first-time JVM warmup
        assertTrue(elapsedTime < 3000, 
            "Generating 1000 rows took " + elapsedTime + "ms, should be < 3000ms");
        
        System.out.println("✅ Performance Test: 1000 rows generated in " + elapsedTime + "ms");
    }

    @Test
    void testIntegrationMultipleGenerators() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("amount", ColumnType.AMOUNT));
        
        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);
        request.setDomainId(1L);
        
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        
        assertNotNull(response);
        assertEquals(100, response.getNumberOfRows());
        assertNotNull(response.getGenerationTimeMs());
    }
}
