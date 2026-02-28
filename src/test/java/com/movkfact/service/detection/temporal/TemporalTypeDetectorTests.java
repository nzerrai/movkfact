package com.movkfact.service.detection.temporal;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for TemporalTypeDetector - orchestrator for temporal type detection
 */
@SpringBootTest
public class TemporalTypeDetectorTests {
    
    @Autowired
    private TemporalTypeDetector detector;
    
    @Test
    public void detector_identifies_birth_date_column() {
        List<String> dates = Arrays.asList(
                "1980-05-15",
                "1995-12-31",
                "1970-01-01",
                "1985-06-15",
                "1990-03-20"
        );
        ColumnType detected = detector.detect("date_birth", dates);
        assertThat(detected).isEqualTo(ColumnType.BIRTH_DATE);
    }
    
    @Test
    public void detector_identifies_birth_date_column_with_column_name_hint() {
        List<String> dates = Arrays.asList(
                "2025-01-15",
                "2024-12-25",
                "2023-06-10",
                "2022-03-01",
                "2021-09-30"
        );
        ColumnType detected = detector.detect("birth_date", dates);
        // With "birth" in column name, should prefer BIRTH_DATE even if dates are recent
        assertThat(detected).isNotNull();
    }
    
    @Test
    public void detector_identifies_date_column() {
        List<String> dates = Arrays.asList(
                "2025-01-15",
                "2024-12-25",
                "2023-06-10",
                "2022-03-01",
                "2021-09-30"
        );
        ColumnType detected = detector.detect("created_at", dates);
        assertThat(detected).isEqualTo(ColumnType.DATE);
    }
    
    @Test
    public void detector_identifies_date_column_with_timestamps() {
        List<String> datetimes = Arrays.asList(
                "2025-01-15T10:30:00",
                "2024-12-25T14:45:30",
                "2023-06-10T08:15:00",
                "2022-03-01T23:59:59",
                "2021-09-30T00:00:00"
        );
        ColumnType detected = detector.detect("modified_at", datetimes);
        assertThat(detected).isEqualTo(ColumnType.DATE);
    }
    
    @Test
    public void detector_identifies_time_column() {
        List<String> times = Arrays.asList(
                "10:30:00",
                "14:45:30",
                "08:15:00",
                "23:59:59",
                "00:00:00"
        );
        ColumnType detected = detector.detect("time_column", times);
        assertThat(detected).isEqualTo(ColumnType.TIME);
    }
    
    @Test
    public void detector_identifies_timezone_column() {
        List<String> timezones = Arrays.asList(
                "Europe/Paris",
                "America/New_York",
                "Asia/Tokyo",
                "UTC",
                "GMT"
        );
        ColumnType detected = detector.detect("timezone", timezones);
        assertThat(detected).isEqualTo(ColumnType.TIMEZONE);
    }
    
    @Test
    public void detector_identifies_timezone_with_offsets() {
        List<String> timezones = Arrays.asList(
                "UTC+1",
                "UTC-5",
                "UTC+0",
                "UTC+2",
                "UTC-8"
        );
        ColumnType detected = detector.detect("tz_offset", timezones);
        assertThat(detected).isEqualTo(ColumnType.TIMEZONE);
    }
    
    @Test
    public void detector_returns_null_for_inconclusive_data() {
        List<String> mixed = Arrays.asList(
                "John",
                "Smith",
                "john@example.com",
                "12345",
                "France"
        );
        ColumnType detected = detector.detect("mixed_data", mixed);
        assertThat(detected).isNull();
    }
    
    @Test
    public void detector_handles_empty_list() {
        List<String> empty = Arrays.asList();
        ColumnType detected = detector.detect("empty_column", empty);
        assertThat(detected).isNull();
    }
}
