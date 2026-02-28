package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for header-based pattern matching.
 * Uses PatternCache for pre-compiled regex patterns.
 * Calculates confidence scores based on pattern matches.
 */
@Service
public class ColumnPatternDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(ColumnPatternDetector.class);
    
    @Autowired
    private PatternCache patternCache;
    
    /**
     * Match column header against all pattern types.
     * Returns confidence scores for each ColumnType that matched.
     * 
     * Confidence = 80 + (20 * matched patterns / total patterns for type)
     * This ensures minimum 80% when any pattern matches (high confidence for obvious matches)
     * 
     * @param columnName The column header name to match
     * @return Map of ColumnType to confidence score (0-100)
     */
    public Map<ColumnType, Integer> matchPatterns(String columnName) {
        Map<ColumnType, Integer> matches = new HashMap<>();
        
        if (columnName == null || columnName.isEmpty()) {
            return matches;
        }
        
        // Get all cached patterns
        Map<ColumnType, List<Pattern>> allPatterns = patternCache.getAllPatterns();
        
        // Test each ColumnType's patterns against the column name
        for (Map.Entry<ColumnType, List<Pattern>> entry : allPatterns.entrySet()) {
            ColumnType columnType = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            if (patterns.isEmpty()) {
                continue;
            }
            
            // Count how many patterns match
            int matchCount = 0;
            List<String> matchedPatterns = new ArrayList<>();
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(columnName);
                if (matcher.find()) {
                    matchCount++;
                    matchedPatterns.add(pattern.pattern());
                }
            }
            
            // Calculate confidence score
            // If at least one pattern matches, confidence is high (80-100)
            // Formula: 80 + (20 * matchCount / totalPatterns) ensures minimum 80% when matched
            if (matchCount > 0) {
                int confidence = 80 + (20 * matchCount) / patterns.size();
                matches.put(columnType, confidence);
                
                logger.debug("ColumnPatternDetector: Column '{}' matched {} / {} patterns for type {} (confidence: {}%)",
                        columnName, matchCount, patterns.size(), columnType.name(), confidence);
            }
        }
        
        return matches;
    }
}
