package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.ColumnConfigurationRepository;
import com.movkfact.repository.ColumnConfigRepository;
import com.movkfact.repository.DataSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColumnConfigurationServiceTest {

    @Mock
    private ColumnConfigurationRepository columnConfigurationRepository;

    @Mock
    private ColumnConfigRepository columnConfigRepository;

    @Mock
    private DataSetRepository dataSetRepository;

    @InjectMocks
    private ColumnConfigurationService columnConfigurationService;

    @Test
    void addExtraColumns_shouldReturnDetectedWhenExtraNull() {
        List<ColumnConfigDTO> detected = List.of(createColumn("name", ColumnType.TEXT));
        
        List<ColumnConfigDTO> result = columnConfigurationService.addExtraColumns(detected, null);
        
        assertEquals(detected, result);
    }

    @Test
    void addExtraColumns_shouldReturnDetectedWhenExtraEmpty() {
        List<ColumnConfigDTO> detected = List.of(createColumn("name", ColumnType.TEXT));
        
        List<ColumnConfigDTO> result = columnConfigurationService.addExtraColumns(detected, List.of());
        
        assertEquals(detected, result);
    }

    @Test
    void addExtraColumns_shouldMergeWhenValid() {
        List<ColumnConfigDTO> detected = List.of(createColumn("name", ColumnType.TEXT));
        List<ColumnConfigDTO> extra = List.of(createColumn("age", ColumnType.INTEGER));
        
        List<ColumnConfigDTO> result = columnConfigurationService.addExtraColumns(detected, extra);
        
        assertEquals(2, result.size());
        assertEquals("name", result.get(0).getName());
        assertEquals("age", result.get(1).getName());
    }

    @Test
    void addExtraColumns_shouldThrowWhenDuplicateName() {
        List<ColumnConfigDTO> detected = List.of(createColumn("name", ColumnType.TEXT));
        List<ColumnConfigDTO> extra = List.of(createColumn("name", ColumnType.INTEGER));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            columnConfigurationService.addExtraColumns(detected, extra));
        
        assertEquals("Duplicate column name: name", exception.getMessage());
    }

    private ColumnConfigDTO createColumn(String name, ColumnType type) {
        ColumnConfigDTO dto = new ColumnConfigDTO();
        dto.setName(name);
        dto.setColumnType(type);
        return dto;
    }
}