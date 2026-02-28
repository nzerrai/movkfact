package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Singleton component that compiles and caches regex patterns at startup.
 * WINSTON RECOMMENDATION: ~100x performance boost vs re-compiling patterns per request.
 * 
 * Loads patterns from patterns.yml at application startup via @PostConstruct,
 * compiles all patterns into Pattern objects, and caches them in memory.
 */
@Component
public class PatternCache {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternCache.class);
    
    private final Map<ColumnType, List<Pattern>> patternCache = new HashMap<>();
    
    @PostConstruct
    public void initializePatterns() {
        logger.info("PatternCache: Initializing - Loading and compiling patterns from patterns.yml");
        
        try {
            // Load patterns.yml from classpath
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass().getClassLoader()
                    .getResourceAsStream("patterns.yml");
            
            if (inputStream == null) {
                logger.error("patterns.yml not found on classpath");
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, List<String>> patternData = yaml.load(inputStream);
            
            // Compile patterns for each ColumnType
            for (ColumnType columnType : ColumnType.values()) {
                List<String> patternStrings = patternData.get(columnType.name());
                
                if (patternStrings != null && !patternStrings.isEmpty()) {
                    List<Pattern> compiledPatterns = new ArrayList<>();
                    for (String patternStr : patternStrings) {
                        try {
                            Pattern compiled = Pattern.compile(patternStr);
                            compiledPatterns.add(compiled);
                        } catch (Exception e) {
                            logger.warn("Failed to compile pattern for {}: {}", columnType, patternStr, e);
                        }
                    }
                    patternCache.put(columnType, compiledPatterns);
                    logger.debug("PatternCache: Compiled {} patterns for {}", 
                            compiledPatterns.size(), columnType.name());
                }
            }
            
            logger.info("PatternCache: Initialization complete - {} types cached", 
                    patternCache.size());
            
        } catch (Exception e) {
            logger.error("Error initializing PatternCache", e);
        }
    }
    
    /**
     * Get pre-compiled patterns for a specific ColumnType.
     * Returns cached {@link java.util.List} of {@link java.util.regex.Pattern} - no recompilation needed.
     * 
     * @param columnType The column type to get patterns for
     * @return List of compiled Pattern objects, or empty list if not found
     */
    public List<Pattern> getPatterns(ColumnType columnType) {
        return patternCache.getOrDefault(columnType, new ArrayList<>());
    }
    
    /**
     * Get all cached patterns.
     * @return Map of ColumnType to compiled Pattern objects
     */
    public Map<ColumnType, List<Pattern>> getAllPatterns() {
        return new HashMap<>(patternCache);
    }
}
