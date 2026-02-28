package com.movkfact.service.detection.personal;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for PersonalTypeDetector - validates all 6 personal types together
 */
@SpringBootTest
public class PersonalTypeDetectorTests {
    
    @Autowired
    private PersonalTypeDetector detector;
    
    @Test
    public void detector_identifies_first_names() {
        List<String> firstNames = Arrays.asList("Jean", "Marie", "John", "Sarah", "Michael");
        ColumnType detected = detector.detect("firstname_column", firstNames);
        assertThat(detected).isEqualTo(ColumnType.FIRST_NAME);
    }
    
    @Test
    public void detector_identifies_last_names() {
        List<String> lastNames = Arrays.asList("Smith", "Johnson", "Martin", "Dupont", "Garcia");
        ColumnType detected = detector.detect("lastname_column", lastNames);
        assertThat(detected).isEqualTo(ColumnType.LAST_NAME);
    }
    
    @Test
    public void detector_identifies_emails() {
        List<String> emails = Arrays.asList(
                "john@example.com",
                "marie@domain.fr",
                "smith@company.com",
                "test@email.org",
                "user@site.net"
        );
        ColumnType detected = detector.detect("email_column", emails);
        assertThat(detected).isEqualTo(ColumnType.EMAIL);
    }
    
    @Test
    public void detector_identifies_genders() {
        List<String> genders = Arrays.asList("M", "F", "M", "F", "M");
        ColumnType detected = detector.detect("gender_column", genders);
        assertThat(detected).isEqualTo(ColumnType.GENDER);
    }
    
    @Test
    public void detector_identifies_phones() {
        List<String> phones = Arrays.asList(
                "202-555-0173",
                "(202) 555-0173",
                "+1-202-555-0173",
                "2025550173",
                "202.555.0173"
        );
        ColumnType detected = detector.detect("phone_column", phones);
        assertThat(detected).isEqualTo(ColumnType.PHONE);
    }
    
    @Test
    public void detector_identifies_addresses() {
        List<String> addresses = Arrays.asList(
                "123 Main Street, New York, NY 10001",
                "456 Avenue, Paris, 75000",
                "789 Elm Road, Chicago, IL 60601",
                "100 Park Lane, Houston, TX 77001",
                "200 Broadway, San Francisco, CA 94102"
        );
        ColumnType detected = detector.detect("address_column", addresses);
        assertThat(detected).isEqualTo(ColumnType.ADDRESS);
    }
    
    @Test
    public void detector_handles_mixed_data() {
        List<String> mixed = Arrays.asList(
                "John",
                "Marie",
                "Sarah",
                "Michael",
                "Jean"
        );
        ColumnType detected = detector.detect("names_column", mixed);
        // Should detect FIRST_NAME (most common names)
        assertThat(detected).isNotNull();
    }
    
    @Test
    public void detector_returns_null_for_unknown_data() {
        List<String> unknown = Arrays.asList(
                "Product ABC",
                "Invoice #123",
                "Report XYZ",
                "Document 456",
                "File 789"
        );
        ColumnType detected = detector.detect("unknown_column", unknown);
        assertThat(detected).isNull();
    }
}
