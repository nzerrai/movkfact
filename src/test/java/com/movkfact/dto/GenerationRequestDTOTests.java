package com.movkfact.dto;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour GenerationRequestDTO.
 */
class GenerationRequestDTOTests {

    @Test
    void testGenerationRequestDTOCreation() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        assertNotNull(request);
    }

    @Test
    void testGenerationRequestDTOWithConstructor() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        
        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);
        
        assertEquals(100, request.getNumberOfRows());
        assertEquals(1, request.getColumns().size());
    }

    @Test
    void testGenerationRequestDTOSettersGetters() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setDomainId(1L);
        request.setDatasetName("TestDataset");
        request.setNumberOfRows(50);
        
        assertEquals(1L, request.getDomainId());
        assertEquals("TestDataset", request.getDatasetName());
        assertEquals(50, request.getNumberOfRows());
    }

    @Test
    void testGenerationRequestDTOWithColumns() {
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("lastname", ColumnType.LAST_NAME));
        
        GenerationRequestDTO request = new GenerationRequestDTO(100, columns);
        
        assertEquals(2, request.getColumns().size());
    }
}
