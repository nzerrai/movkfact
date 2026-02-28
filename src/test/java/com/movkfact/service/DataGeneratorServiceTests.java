package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DataGeneratorService.
 */
@SpringBootTest
class DataGeneratorServiceTests {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    private GenerationRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("lastname", ColumnType.LAST_NAME));
        
        validRequest = new GenerationRequestDTO(100, columns);
        validRequest.setDomainId(1L);
        validRequest.setDatasetName("TestDataset");
    }

    @Test
    void testDataGeneratorServiceIsNotNull() {
        assertNotNull(dataGeneratorService);
    }

    @Test
    void testGenerateWithValidRequest() {
        GenerationResponseDTO response = dataGeneratorService.generate(validRequest);
        
        assertNotNull(response);
        assertEquals(100, response.getNumberOfRows());
        assertNotNull(response.getGenerationTimeMs());
        assertTrue(response.getGenerationTimeMs() >= 0);
    }

    @Test
    void testGenerateThrowsExceptionWithNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> dataGeneratorService.generate(null));
    }

    @Test
    void testGenerateThrowsExceptionWithNullColumns() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(100);
        request.setColumns(null);
        
        assertThrows(IllegalArgumentException.class, () -> dataGeneratorService.generate(request));
    }

    @Test
    void testGenerateThrowsExceptionWithNullNumberOfRows() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setColumns(new ArrayList<>());
        request.setNumberOfRows(null);
        
        assertThrows(IllegalArgumentException.class, () -> dataGeneratorService.generate(request));
    }

    @Test
    void testGenerateThrowsExceptionWithZeroRows() {
        GenerationRequestDTO request = new GenerationRequestDTO(0, new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> dataGeneratorService.generate(request));
    }

    @Test
    void testGenerateThrowsExceptionWithNegativeRows() {
        GenerationRequestDTO request = new GenerationRequestDTO(-5, new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> dataGeneratorService.generate(request));
    }

    @Test
    void testPerformanceFor1000Rows() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        
        GenerationRequestDTO request = new GenerationRequestDTO(1000, columns);
        request.setDomainId(1L);
        
        long startTime = System.currentTimeMillis();
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        long callTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(response);
        assertEquals(1000, response.getNumberOfRows());
        // Performance test: generation should be fast (allowing for initial load)
        assertTrue(callTime < 5000, "Generation took " + callTime + "ms, should be < 5000ms");
    }
}
