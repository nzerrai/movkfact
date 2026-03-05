package com.movkfact.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.DomainRepository;
import com.movkfact.service.DataGeneratorService;
import org.springframework.batch.item.ItemProcessor;

/**
 * Processor Spring Batch : génère un DataSet à partir d'une config.
 * Appelé en parallèle par plusieurs threads (thread-safe car stateless).
 * Sujet au retry logic (3 tentatives avec backoff exponentiel).
 */
public class DataSetItemProcessor implements ItemProcessor<BatchDataSetConfigDTO, DataSet> {

    private final DataGeneratorService dataGeneratorService;
    private final DomainRepository domainRepository;
    private final ObjectMapper objectMapper;

    public DataSetItemProcessor(DataGeneratorService dataGeneratorService,
                                 DomainRepository domainRepository,
                                 ObjectMapper objectMapper) {
        this.dataGeneratorService = dataGeneratorService;
        this.domainRepository = domainRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public DataSet process(BatchDataSetConfigDTO config) throws Exception {
        // Validate domain exists
        domainRepository.findByIdAndDeletedAtIsNull(config.getDomainId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Domain not found with id: " + config.getDomainId()));

        // Build generation request
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setDomainId(config.getDomainId());
        request.setDatasetName(config.getDatasetName());
        request.setNumberOfRows(config.getCount());
        request.setColumns(config.getColumns());

        // Generate data
        GenerationResponseDTO response = dataGeneratorService.generate(request);

        // Build DataSet entity (not yet persisted)
        DataSet dataset = new DataSet();
        dataset.setDomainId(config.getDomainId());
        dataset.setName(config.getDatasetName());
        dataset.setRowCount(response.getNumberOfRows());
        dataset.setColumnCount(config.getColumns().size());
        dataset.setGenerationTimeMs(response.getGenerationTimeMs());

        String dataJson = objectMapper.writeValueAsString(response.getData());
        dataset.setDataJson(dataJson);
        dataset.setOriginalData(dataJson);
        dataset.setVersion(0);

        return dataset;
    }
}
