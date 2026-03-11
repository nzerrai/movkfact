package com.movkfact.service;

import com.movkfact.entity.SystemConfiguration;
import com.movkfact.repository.SystemConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {
    
    @Mock
    private SystemConfigurationRepository systemConfigurationRepository;
    
    @InjectMocks
    private ConfigurationService configurationService;
    
    @Test
    void getConfiguration_shouldReturnIfExists() {
        SystemConfiguration config = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.of(config));
        
        Optional<SystemConfiguration> result = configurationService.getConfiguration("max_columns");
        
        assertTrue(result.isPresent());
        assertEquals("50", result.get().getConfigValue());
        verify(systemConfigurationRepository, times(1)).findByConfigKey("max_columns");
    }
    
    @Test
    void getConfigurationValue_shouldReturnValue() {
        SystemConfiguration config = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.of(config));
        
        String result = configurationService.getConfigurationValue("max_columns", "100");
        
        assertEquals("50", result);
    }
    
    @Test
    void getConfigurationValue_shouldReturnDefaultIfNotExists() {
        when(systemConfigurationRepository.findByConfigKey("nonexistent")).thenReturn(Optional.empty());
        
        String result = configurationService.getConfigurationValue("nonexistent", "default");
        
        assertEquals("default", result);
    }
    
    @Test
    void getConfigurationAsInteger_shouldParseCorrectly() {
        SystemConfiguration config = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.of(config));
        
        Integer result = configurationService.getConfigurationAsInteger("max_columns", 100);
        
        assertEquals(50, result);
    }
    
    @Test
    void getConfigurationAsInteger_shouldReturnDefaultIfInvalid() {
        SystemConfiguration config = new SystemConfiguration("invalid", "abc", "Invalid value", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("invalid")).thenReturn(Optional.of(config));
        
        Integer result = configurationService.getConfigurationAsInteger("invalid", 100);
        
        assertEquals(100, result);
    }
    
    @Test
    void getConfigurationAsBoolean_shouldParseCorrectly() {
        SystemConfiguration config = new SystemConfiguration("feature_enabled", "true", "Feature flag", "BOOLEAN");
        when(systemConfigurationRepository.findByConfigKey("feature_enabled")).thenReturn(Optional.of(config));
        
        Boolean result = configurationService.getConfigurationAsBoolean("feature_enabled", false);
        
        assertTrue(result);
    }
    
    @Test
    void saveConfiguration_shouldCreateNew() {
        SystemConfiguration config = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.empty());
        when(systemConfigurationRepository.save(any(SystemConfiguration.class))).thenReturn(config);
        
        SystemConfiguration result = configurationService.saveConfiguration("max_columns", "50", "Max columns", "INTEGER");
        
        assertNotNull(result);
        assertEquals("50", result.getConfigValue());
        verify(systemConfigurationRepository, times(1)).save(any(SystemConfiguration.class));
    }
    
    @Test
    void saveConfiguration_shouldUpdateExisting() {
        SystemConfiguration existing = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        SystemConfiguration updated = new SystemConfiguration("max_columns", "100", "Updated max columns", "INTEGER");
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.of(existing));
        when(systemConfigurationRepository.save(any(SystemConfiguration.class))).thenReturn(updated);
        
        SystemConfiguration result = configurationService.saveConfiguration("max_columns", "100", "Updated max columns", "INTEGER");
        
        assertEquals("100", result.getConfigValue());
        verify(systemConfigurationRepository, times(1)).save(any(SystemConfiguration.class));
    }
    
    @Test
    void deleteConfiguration_shouldDelete() {
        SystemConfiguration config = new SystemConfiguration("max_columns", "50", "Max columns", "INTEGER");
        config.setId(1L);
        when(systemConfigurationRepository.findByConfigKey("max_columns")).thenReturn(Optional.of(config));
        
        configurationService.deleteConfiguration("max_columns");
        
        verify(systemConfigurationRepository, times(1)).deleteById(1L);
    }
}