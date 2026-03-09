package com.movkfact.batch;

import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.event.DatasetActivityEvent;
import com.movkfact.repository.DataSetRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Writer Spring Batch : persiste les datasets générés.
 * Publie un événement CREATED pour chaque dataset sauvegardé.
 * Stateless par rapport au jobId — thread-safe.
 */
public class DataSetItemWriter implements ItemWriter<DataSet> {

    private final DataSetRepository dataSetRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DataSetItemWriter(DataSetRepository dataSetRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.dataSetRepository = dataSetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void write(Chunk<? extends DataSet> chunk) throws Exception {
        for (DataSet dataset : chunk.getItems()) {
            dataset.setName(resolveUniqueName(dataset.getDomainId(), dataset.getName()));
            DataSet saved = dataSetRepository.save(dataset);
            eventPublisher.publishEvent(
                new DatasetActivityEvent(saved.getId(), ActivityActionType.CREATED, "batch")
            );
        }
    }

    /**
     * Returns {@code baseName} if no active dataset with that name exists for the domain,
     * otherwise appends "_2", "_3", … until a free slot is found.
     */
    private String resolveUniqueName(Long domainId, String baseName) {
        if (!dataSetRepository.existsByDomainIdAndNameAndDeletedAtIsNull(domainId, baseName)) {
            return baseName;
        }
        int counter = 2;
        while (true) {
            String candidate = baseName + "_" + counter;
            if (!dataSetRepository.existsByDomainIdAndNameAndDeletedAtIsNull(domainId, candidate)) {
                return candidate;
            }
            counter++;
        }
    }
}
