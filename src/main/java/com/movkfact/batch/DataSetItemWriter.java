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
            DataSet saved = dataSetRepository.save(dataset);
            eventPublisher.publishEvent(
                new DatasetActivityEvent(saved.getId(), ActivityActionType.CREATED, "batch")
            );
        }
    }
}
