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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Domain business logic.
 * Handles aggregated stats (FR-002) and enriched dataset listings (FR-003).
 * Anti-N+1: all activities loaded in a single query per domain batch.
 */
@Service
public class DomainService {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * Returns all active domains with aggregated stats (datasetCount, totalRows, statuses).
     * Pagination applied in-memory on the full list (consistent with existing DomainController behaviour).
     * Anti-N+1: datasets and activities loaded in bulk.
     *
     * @param offset starting index (0-based)
     * @param limit  max items to return
     * @return paginated list of DomainResponseDTO with stats
     */
    public List<DomainResponseDTO> getDomainsWithStats(int offset, int limit) {
        List<Domain> all = domainRepository.findByDeletedAtIsNull();

        int from = Math.min(offset, all.size());
        int to   = Math.min(offset + limit, all.size());
        List<Domain> page = all.subList(from, to);

        if (page.isEmpty()) return Collections.emptyList();

        List<Long> domainIds = page.stream().map(Domain::getId).collect(Collectors.toList());

        // Load all datasets for these domains in one query — avoids N+1
        List<DataSet> allDatasets = dataSetRepository.findByDomainIdInAndDeletedAtIsNull(domainIds);
        Map<Long, List<DataSet>> datasetsByDomain = allDatasets.stream()
                .collect(Collectors.groupingBy(DataSet::getDomainId));

        // Load all activities for all datasets in one query — avoids N+1
        List<Long> allDsIds = allDatasets.stream().map(DataSet::getId).collect(Collectors.toList());
        Map<Long, List<Activity>> activitiesByDataset = allDsIds.isEmpty()
                ? Collections.emptyMap()
                : activityRepository.findByDataSetIdIn(allDsIds).stream()
                        .collect(Collectors.groupingBy(Activity::getDataSetId));

        return page.stream().map(domain -> {
            List<DataSet> datasets = datasetsByDomain.getOrDefault(domain.getId(), Collections.emptyList());

            DomainStatusDTO statuses = computeDomainStatus(datasets, activitiesByDataset);
            long totalRows = datasets.stream()
                    .mapToLong(ds -> ds.getRowCount() != null ? ds.getRowCount() : 0L)
                    .sum();

            DomainResponseDTO dto = mapToDTO(domain);
            dto.setDatasetCount(datasets.size());
            dto.setTotalRows(totalRows);
            dto.setStatuses(statuses);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Returns datasets for a given domain, enriched with per-dataset status and lastActivity.
     * Sorted by updatedAt DESC.
     * Anti-N+1: activities loaded in a single query.
     *
     * @param domainId the domain ID
     * @return list of DataSetSummaryDTO sorted by updatedAt DESC
     * @throws EntityNotFoundException if domain does not exist
     */
    public List<DataSetSummaryDTO> getDatasetsByDomainWithStats(Long domainId) {
        domainRepository.findByIdAndDeletedAtIsNull(domainId)
                .orElseThrow(() -> new EntityNotFoundException("Domain not found with ID: " + domainId));

        List<DataSet> datasets = dataSetRepository.findByDomainIdAndDeletedAtIsNull(domainId);
        if (datasets.isEmpty()) return Collections.emptyList();

        List<Long> dsIds = datasets.stream().map(DataSet::getId).collect(Collectors.toList());

        // Load all activities for these datasets in one query
        List<Activity> activities = activityRepository.findByDataSetIdIn(dsIds);

        Map<Long, List<Activity>> actsByDataset = activities.stream()
                .collect(Collectors.groupingBy(Activity::getDataSetId));

        return datasets.stream()
                .sorted(Comparator.comparing(DataSet::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ds -> {
                    List<Activity> dsActivities = actsByDataset.getOrDefault(ds.getId(), Collections.emptyList());
                    DomainStatusDTO status = computeDatasetStatus(ds, dsActivities);
                    LocalDateTime lastActivity = dsActivities.stream()
                            .map(Activity::getTimestamp)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(null);

                    return new DataSetSummaryDTO(
                            ds.getId(),
                            ds.getName(),
                            ds.getRowCount(),
                            ds.getColumnCount(),
                            ds.getCreatedAt(),
                            ds.getUpdatedAt(),
                            status,
                            lastActivity
                    );
                })
                .collect(Collectors.toList());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Computes aggregated status for a domain: OR-logic across all its datasets.
     */
    private DomainStatusDTO computeDomainStatus(List<DataSet> datasets,
                                                Map<Long, List<Activity>> activitiesByDataset) {
        if (datasets.isEmpty()) return new DomainStatusDTO(false, false, false);

        Set<ActivityActionType> actions = datasets.stream()
                .flatMap(ds -> activitiesByDataset.getOrDefault(ds.getId(), Collections.emptyList()).stream())
                .map(Activity::getAction)
                .collect(Collectors.toSet());

        boolean downloaded = actions.contains(ActivityActionType.DOWNLOADED);
        boolean modified   = datasets.stream().anyMatch(ds -> ds.getVersion() != null && ds.getVersion() > 0);
        boolean viewed     = actions.contains(ActivityActionType.VIEWED);

        return new DomainStatusDTO(downloaded, modified, viewed);
    }

    /**
     * Computes status for a single dataset.
     */
    private DomainStatusDTO computeDatasetStatus(DataSet ds, List<Activity> activities) {
        Set<ActivityActionType> actions = activities.stream()
                .map(Activity::getAction)
                .collect(Collectors.toSet());

        boolean downloaded = actions.contains(ActivityActionType.DOWNLOADED);
        boolean modified   = ds.getVersion() != null && ds.getVersion() > 0;
        boolean viewed     = actions.contains(ActivityActionType.VIEWED);

        return new DomainStatusDTO(downloaded, modified, viewed);
    }

    private DomainResponseDTO mapToDTO(Domain domain) {
        return new DomainResponseDTO(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt()
        );
    }
}
