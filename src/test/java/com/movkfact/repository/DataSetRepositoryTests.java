package com.movkfact.repository;

import com.movkfact.entity.DataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DataSetRepository.
 * Tests custom query methods for soft-delete functionality.
 */
@DataJpaTest
class DataSetRepositoryTests {

    @Autowired
    private DataSetRepository dataSetRepository;

    private DataSet activeDataSet;
    private DataSet deletedDataSet;
    
    private static final Long TEST_DOMAIN_ID = 1L;
    private static final Long OTHER_DOMAIN_ID = 2L;

    @BeforeEach
    void setUp() {
        // Create active dataset (no deletion)
        activeDataSet = new DataSet();
        activeDataSet.setDomainId(TEST_DOMAIN_ID);
        activeDataSet.setName("Active Dataset");
        activeDataSet.setRowCount(100);
        activeDataSet.setGenerationTimeMs(500L);
        activeDataSet.setDataJson("[{\"col1\": \"val1\"}]");
        dataSetRepository.save(activeDataSet);

        // Create deleted dataset (soft delete)
        deletedDataSet = new DataSet();
        deletedDataSet.setDomainId(TEST_DOMAIN_ID);
        deletedDataSet.setName("Deleted Dataset");
        deletedDataSet.setRowCount(50);
        deletedDataSet.setGenerationTimeMs(250L);
        deletedDataSet.setDataJson("[{\"col1\": \"val2\"}]");
        deletedDataSet.setDeletedAt(LocalDateTime.now());
        dataSetRepository.save(deletedDataSet);
    }

    @Test
    void testFindByDomainIdAndDeletedAtIsNull_ReturnsOnlyActiveDatasets() {
        List<DataSet> result = dataSetRepository.findByDomainIdAndDeletedAtIsNull(TEST_DOMAIN_ID);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeDataSet.getId(), result.get(0).getId());
        assertEquals("Active Dataset", result.get(0).getName());
    }

    @Test
    void testFindByDomainIdAndDeletedAtIsNull_ReturnsEmptyForNonExistentDomain() {
        List<DataSet> result = dataSetRepository.findByDomainIdAndDeletedAtIsNull(999L);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByDomainIdAndDeletedAtIsNull_ExcludesSoftDeletedDatasets() {
        List<DataSet> result = dataSetRepository.findByDomainIdAndDeletedAtIsNull(TEST_DOMAIN_ID);
        
        // Should only have 1 (the active one), not the deleted one
        assertEquals(1, result.size());
        assertTrue(result.stream().noneMatch(ds -> ds.getDeletedAt() != null));
    }

    @Test
    void testFindByIdAndDeletedAtIsNull_ReturnsActiveDataset() {
        Optional<DataSet> result = dataSetRepository.findByIdAndDeletedAtIsNull(activeDataSet.getId());
        
        assertTrue(result.isPresent());
        assertEquals(activeDataSet.getId(), result.get().getId());
        assertEquals("Active Dataset", result.get().getName());
    }

    @Test
    void testFindByIdAndDeletedAtIsNull_ReturnsEmptyForDeletedDataset() {
        Optional<DataSet> result = dataSetRepository.findByIdAndDeletedAtIsNull(deletedDataSet.getId());
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdAndDeletedAtIsNull_ReturnsEmptyForNonExistentId() {
        Optional<DataSet> result = dataSetRepository.findByIdAndDeletedAtIsNull(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testExistsByIdAndDeletedAtIsNull_ReturnsTrueForActiveDataset() {
        boolean exists = dataSetRepository.existsByIdAndDeletedAtIsNull(activeDataSet.getId());
        
        assertTrue(exists);
    }

    @Test
    void testExistsByIdAndDeletedAtIsNull_ReturnsFalseForDeletedDataset() {
        boolean exists = dataSetRepository.existsByIdAndDeletedAtIsNull(deletedDataSet.getId());
        
        assertFalse(exists);
    }

    @Test
    void testExistsByIdAndDeletedAtIsNull_ReturnsFalseForNonExistentId() {
        boolean exists = dataSetRepository.existsByIdAndDeletedAtIsNull(999L);
        
        assertFalse(exists);
    }

    @Test
    void testFindByDomainIdAndDeletedAtIsNull_WithMultipleDatasets() {
        // Add another active dataset
        DataSet anotherActive = new DataSet();
        anotherActive.setDomainId(TEST_DOMAIN_ID);
        anotherActive.setName("Another Active Dataset");
        anotherActive.setRowCount(200);
        anotherActive.setGenerationTimeMs(1000L);
        anotherActive.setDataJson("[{\"col1\": \"val3\"}]");
        dataSetRepository.save(anotherActive);

        List<DataSet> result = dataSetRepository.findByDomainIdAndDeletedAtIsNull(TEST_DOMAIN_ID);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ds -> !TEST_DOMAIN_ID.equals(ds.getDomainId()) || ds.getDeletedAt() == null));
    }

    @Test
    void testFindByDomainIdAndDeletedAtIsNull_SeparatesByDomain() {
        // Add a dataset for OTHER_DOMAIN
        DataSet otherDomainDataSet = new DataSet();
        otherDomainDataSet.setDomainId(OTHER_DOMAIN_ID);
        otherDomainDataSet.setName("Other Domain Dataset");
        otherDomainDataSet.setRowCount(75);
        otherDomainDataSet.setGenerationTimeMs(350L);
        otherDomainDataSet.setDataJson("[{\"col1\": \"val4\"}]");
        dataSetRepository.save(otherDomainDataSet);

        List<DataSet> testDomainResult = dataSetRepository.findByDomainIdAndDeletedAtIsNull(TEST_DOMAIN_ID);
        List<DataSet> otherDomainResult = dataSetRepository.findByDomainIdAndDeletedAtIsNull(OTHER_DOMAIN_ID);
        
        assertEquals(1, testDomainResult.size());
        assertEquals(1, otherDomainResult.size());
        assertEquals(TEST_DOMAIN_ID, testDomainResult.get(0).getDomainId());
        assertEquals(OTHER_DOMAIN_ID, otherDomainResult.get(0).getDomainId());
    }

    @Test
    void testDataSetPersistence_SavesAndRetrievesWithMetadata() {
        DataSet newDataSet = new DataSet();
        newDataSet.setDomainId(TEST_DOMAIN_ID);
        newDataSet.setName("Test Persistence");
        newDataSet.setRowCount(150);
        newDataSet.setGenerationTimeMs(750L);
        newDataSet.setDataJson("[{\"test\": \"data\"}]");

        DataSet saved = dataSetRepository.save(newDataSet);
        assertNotNull(saved.getId());

        Optional<DataSet> retrieved = dataSetRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Test Persistence", retrieved.get().getName());
        assertEquals(150, retrieved.get().getRowCount());
        assertEquals(750L, retrieved.get().getGenerationTimeMs());
        assertNotNull(retrieved.get().getCreatedAt());
        assertNotNull(retrieved.get().getUpdatedAt());
    }
}
