package com.movkfact.service.detection.personal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AddressValidator - validates address patterns and keywords
 */
@SpringBootTest
public class AddressValidatorTests {
    
    @Autowired
    private AddressValidator validator;
    
    @Test
    public void validator_detects_us_addresses() {
        List<String> usAddresses = Arrays.asList(
                "123 Main Street, New York, NY 10001",
                "456 Oak Avenue, Los Angeles, CA 90001",
                "789 Elm Road, Chicago, IL 60601",
                "100 Park Lane, Houston, TX 77001",
                "200 Broadway, San Francisco, CA 94102"
        );
        double confidence = validator.validate(usAddresses);
        assertThat(confidence).isGreaterThanOrEqualTo(80.0);
    }
    
    @Test
    public void validator_detects_french_addresses() {
        List<String> frAddresses = Arrays.asList(
                "123 rue de la Paix, Paris, 75000",
                "456 avenue des Champs, Lyon, 69000",
                "789 boulevard Saint-Germain, Paris, 75005",
                "100 place de la Concorde, Paris, 75008",
                "200 quai de la Seine, Paris, 75004"
        );
        double confidence = validator.validate(frAddresses);
        assertThat(confidence).isGreaterThanOrEqualTo(80.0);
    }
    
    @Test
    public void validator_detects_addresses_with_keywords() {
        List<String> addresses = Arrays.asList(
                "123 Street Address",
                "456 Avenue Name",
                "789 Boulevard Lane",
                "100 Road Way",
                "200 Court Circle"
        );
        double confidence = validator.validate(addresses);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_non_address_data() {
        List<String> nonAddress = Arrays.asList(
                "John Smith",
                "john@example.com",
                "555-1234",
                "Product ABC",
                "Invoice #12345"
        );
        double confidence = validator.validate(nonAddress);
        assertThat(confidence).isLessThan(40.0);
    }
    
    @Test
    public void validator_handles_partial_addresses() {
        List<String> partialAddresses = Arrays.asList(
                "New York, NY",
                "Paris, France",
                "London, UK",
                "Toronto, ON",
                "Berlin, Germany"
        );
        double confidence = validator.validate(partialAddresses);
        assertThat(confidence).isGreaterThanOrEqualTo(60.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
}
