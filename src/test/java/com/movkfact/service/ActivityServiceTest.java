package com.movkfact.service;

import com.movkfact.entity.Activity;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.ActivityRepository;
import com.movkfact.repository.DataSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DataSetRepository dataSetRepository;

    @InjectMocks
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRecordActivity() {
        // Given
        Long datasetId = 1L;
        ActivityActionType action = ActivityActionType.CREATED;
        String userName = "testUser";
        Activity activity = new Activity(datasetId, action, userName);
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        // When
        Activity result = activityService.recordActivity(datasetId, action, userName);

        // Then
        assertNotNull(result);
        assertEquals(datasetId, result.getDataSetId());
        assertEquals(action, result.getAction());
        assertEquals(userName, result.getUserName());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void testGetActivityHistory() {
        // Given
        Long datasetId = 1L;
        List<Activity> activities = Arrays.asList(
            new Activity(datasetId, ActivityActionType.CREATED, "user1"),
            new Activity(datasetId, ActivityActionType.VIEWED, "user2")
        );
        when(activityRepository.findByDataSetIdOrderByTimestampDesc(datasetId)).thenReturn(activities);

        // When
        List<Activity> result = activityService.getActivityHistory(datasetId);

        // Then
        assertEquals(2, result.size());
        verify(activityRepository, times(1)).findByDataSetIdOrderByTimestampDesc(datasetId);
    }

    @Test
    void testGetActivityByType() {
        // Given
        Long datasetId = 1L;
        ActivityActionType action = ActivityActionType.VIEWED;
        List<Activity> activities = Arrays.asList(
            new Activity(datasetId, action, "user1")
        );
        when(activityRepository.findByDataSetIdAndActionOrderByTimestampDesc(datasetId, action)).thenReturn(activities);

        // When
        List<Activity> result = activityService.getActivityByType(datasetId, action);

        // Then
        assertEquals(1, result.size());
        assertEquals(action, result.get(0).getAction());
        verify(activityRepository, times(1)).findByDataSetIdAndActionOrderByTimestampDesc(datasetId, action);
    }

    @Test
    void testResetDataSet() {
        // Given
        Long datasetId = 1L;
        DataSet dataset = new DataSet();
        dataset.setId(datasetId);
        dataset.setDataJson("old data");
        dataset.setOriginalData("original data");
        dataset.setVersion(1);
        
        when(dataSetRepository.findByIdAndDeletedAtIsNull(datasetId)).thenReturn(java.util.Optional.of(dataset));
        when(dataSetRepository.save(any(DataSet.class))).thenReturn(dataset);

        // When
        DataSet result = activityService.resetDataSet(datasetId);

        // Then
        assertNotNull(result);
        assertEquals("original data", result.getDataJson());
        assertEquals(0, result.getVersion());
        verify(dataSetRepository, times(1)).findByIdAndDeletedAtIsNull(datasetId);
        verify(dataSetRepository, times(1)).save(dataset);
    }

    @Test
    void testResetDataSetNotFound() {
        // Given
        Long datasetId = 1L;
        when(dataSetRepository.findByIdAndDeletedAtIsNull(datasetId)).thenReturn(java.util.Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            activityService.resetDataSet(datasetId);
        });
        assertEquals("Dataset not found with id: 1", exception.getMessage());
    }

    @Test
    void testResetDataSetNoOriginalData() {
        // Given
        Long datasetId = 1L;
        DataSet dataset = new DataSet();
        dataset.setId(datasetId);
        dataset.setOriginalData(null);
        
        when(dataSetRepository.findByIdAndDeletedAtIsNull(datasetId)).thenReturn(java.util.Optional.of(dataset));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            activityService.resetDataSet(datasetId);
        });
        assertEquals("Original data not found for dataset: 1", exception.getMessage());
    }
}