package com.movkfact.service;

import com.movkfact.dto.DataSetSummaryDTO;
import com.movkfact.dto.DomainResponseDTO;
import com.movkfact.dto.DomainStatusDTO;
import com.movkfact.entity.Activity;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.entity.Domain;
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.repository.ActivityRepository;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class DomainServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private DataSetRepository dataSetRepository;
    @Mock private ActivityRepository activityRepository;

    @InjectMocks
    private DomainService domainService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Domain domain(Long id, String name) {
        Domain d = new Domain(name, "desc");
        d.setId(id);
        return d;
    }

    private DataSet dataset(Long id, Long domainId, int rowCount, int version) {
        DataSet ds = new DataSet();
        ds.setId(id);
        ds.setDomainId(domainId);
        ds.setRowCount(rowCount);
        ds.setVersion(version);
        ds.setColumnCount(2);
        ds.setGenerationTimeMs(100L);
        ds.setName("ds-" + id);
        return ds;
    }

    private Activity activity(Long dataSetId, ActivityActionType action) {
        Activity a = new Activity(dataSetId, action, "test");
        return a;
    }

    // ── getDomainsWithStats ───────────────────────────────────────────────────

    @Test
    void getDomainsWithStats_noDomains_returnsEmpty() {
        when(domainRepository.findByDeletedAtIsNull()).thenReturn(Collections.emptyList());
        assertThat(domainService.getDomainsWithStats(0, 100)).isEmpty();
    }

    @Test
    void getDomainsWithStats_domainWithNoDatasets_returnsZeroStats() {
        Domain d = domain(1L, "D1");
        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(List.of(1L))).thenReturn(Collections.emptyList());

        List<DomainResponseDTO> result = domainService.getDomainsWithStats(0, 100);

        assertThat(result).hasSize(1);
        DomainResponseDTO dto = result.get(0);
        assertThat(dto.getDatasetCount()).isEqualTo(0);
        assertThat(dto.getTotalRows()).isEqualTo(0L);
        assertThat(dto.getStatuses().isDownloaded()).isFalse();
        assertThat(dto.getStatuses().isModified()).isFalse();
        assertThat(dto.getStatuses().isViewed()).isFalse();
    }

    @Test
    void getDomainsWithStats_downloaded_flagSetCorrectly() {
        Domain d = domain(1L, "D1");
        DataSet ds = dataset(10L, 1L, 500, 0);

        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(anyList())).thenReturn(List.of(ds));
        when(activityRepository.findByDataSetIdIn(anyList()))
                .thenReturn(List.of(activity(10L, ActivityActionType.DOWNLOADED)));

        List<DomainResponseDTO> result = domainService.getDomainsWithStats(0, 100);

        DomainStatusDTO statuses = result.get(0).getStatuses();
        assertThat(statuses.isDownloaded()).isTrue();
        assertThat(statuses.isModified()).isFalse();
        assertThat(statuses.isViewed()).isFalse();
    }

    @Test
    void getDomainsWithStats_modified_whenVersionGreaterThanZero() {
        Domain d = domain(1L, "D1");
        DataSet ds = dataset(10L, 1L, 100, 2); // version=2 → modified

        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(anyList())).thenReturn(List.of(ds));
        when(activityRepository.findByDataSetIdIn(anyList())).thenReturn(Collections.emptyList());

        DomainStatusDTO statuses = domainService.getDomainsWithStats(0, 100).get(0).getStatuses();
        assertThat(statuses.isModified()).isTrue();
        assertThat(statuses.isDownloaded()).isFalse();
        assertThat(statuses.isViewed()).isFalse();
    }

    @Test
    void getDomainsWithStats_viewed_flagSetCorrectly() {
        Domain d = domain(1L, "D1");
        DataSet ds = dataset(10L, 1L, 200, 0);

        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(anyList())).thenReturn(List.of(ds));
        when(activityRepository.findByDataSetIdIn(anyList()))
                .thenReturn(List.of(activity(10L, ActivityActionType.VIEWED)));

        DomainStatusDTO statuses = domainService.getDomainsWithStats(0, 100).get(0).getStatuses();
        assertThat(statuses.isViewed()).isTrue();
    }

    @Test
    void getDomainsWithStats_totalRows_sumsAllDatasets() {
        Domain d = domain(1L, "D1");
        DataSet ds1 = dataset(10L, 1L, 300, 0);
        DataSet ds2 = dataset(11L, 1L, 700, 0);

        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(anyList())).thenReturn(List.of(ds1, ds2));
        when(activityRepository.findByDataSetIdIn(anyList())).thenReturn(Collections.emptyList());

        DomainResponseDTO dto = domainService.getDomainsWithStats(0, 100).get(0);
        assertThat(dto.getDatasetCount()).isEqualTo(2);
        assertThat(dto.getTotalRows()).isEqualTo(1000L);
    }

    @Test
    void getDomainsWithStats_pagination_appliedCorrectly() {
        Domain d1 = domain(1L, "D1");
        Domain d2 = domain(2L, "D2");
        Domain d3 = domain(3L, "D3");

        when(domainRepository.findByDeletedAtIsNull()).thenReturn(List.of(d1, d2, d3));
        when(dataSetRepository.findByDomainIdInAndDeletedAtIsNull(anyList())).thenReturn(Collections.emptyList());

        // offset=1, limit=1 → only D2
        List<DomainResponseDTO> result = domainService.getDomainsWithStats(1, 1);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("D2");
    }

    // ── getDatasetsByDomainWithStats ──────────────────────────────────────────

    @Test
    void getDatasetsByDomainWithStats_domainNotFound_throwsEntityNotFoundException() {
        when(domainRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domainService.getDatasetsByDomainWithStats(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getDatasetsByDomainWithStats_noDatasets_returnsEmpty() {
        Domain d = domain(1L, "D1");
        when(domainRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(d));
        when(dataSetRepository.findByDomainIdAndDeletedAtIsNull(1L)).thenReturn(Collections.emptyList());

        assertThat(domainService.getDatasetsByDomainWithStats(1L)).isEmpty();
    }

    @Test
    void getDatasetsByDomainWithStats_statusFlags_computedPerDataset() {
        Domain d = domain(1L, "D1");
        DataSet ds = dataset(10L, 1L, 50, 1); // version=1 → modified
        ReflectionTestUtils.setField(ds, "updatedAt", LocalDateTime.now());

        when(domainRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(d));
        when(dataSetRepository.findByDomainIdAndDeletedAtIsNull(1L)).thenReturn(List.of(ds));
        when(activityRepository.findByDataSetIdIn(List.of(10L)))
                .thenReturn(List.of(
                        activity(10L, ActivityActionType.DOWNLOADED),
                        activity(10L, ActivityActionType.VIEWED)
                ));

        List<DataSetSummaryDTO> result = domainService.getDatasetsByDomainWithStats(1L);

        assertThat(result).hasSize(1);
        DomainStatusDTO status = result.get(0).getStatus();
        assertThat(status.isDownloaded()).isTrue();
        assertThat(status.isModified()).isTrue();
        assertThat(status.isViewed()).isTrue();
    }

    @Test
    void getDatasetsByDomainWithStats_lastActivity_isMaxTimestamp() {
        Domain d = domain(1L, "D1");
        DataSet ds = dataset(10L, 1L, 10, 0);
        ReflectionTestUtils.setField(ds, "updatedAt", LocalDateTime.now());

        LocalDateTime older  = LocalDateTime.now().minusDays(5);
        LocalDateTime newer  = LocalDateTime.now().minusDays(1);

        Activity a1 = activity(10L, ActivityActionType.VIEWED);
        a1.setTimestamp(older);
        Activity a2 = activity(10L, ActivityActionType.DOWNLOADED);
        a2.setTimestamp(newer);

        when(domainRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(d));
        when(dataSetRepository.findByDomainIdAndDeletedAtIsNull(1L)).thenReturn(List.of(ds));
        when(activityRepository.findByDataSetIdIn(anyList())).thenReturn(List.of(a1, a2));

        DataSetSummaryDTO dto = domainService.getDatasetsByDomainWithStats(1L).get(0);
        assertThat(dto.getLastActivity()).isEqualTo(newer);
    }

    @Test
    void getDatasetsByDomainWithStats_sortedByUpdatedAtDesc() {
        Domain d = domain(1L, "D1");
        DataSet ds1 = dataset(10L, 1L, 10, 0);
        DataSet ds2 = dataset(11L, 1L, 20, 0);
        ReflectionTestUtils.setField(ds1, "updatedAt", LocalDateTime.now().minusDays(3));
        ReflectionTestUtils.setField(ds2, "updatedAt", LocalDateTime.now().minusDays(1)); // newer

        when(domainRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(d));
        when(dataSetRepository.findByDomainIdAndDeletedAtIsNull(1L)).thenReturn(List.of(ds1, ds2));
        when(activityRepository.findByDataSetIdIn(anyList())).thenReturn(Collections.emptyList());

        List<DataSetSummaryDTO> result = domainService.getDatasetsByDomainWithStats(1L);
        // ds2 (newer updatedAt) should come first
        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(1).getId()).isEqualTo(10L);
    }
}
