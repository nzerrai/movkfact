package com.movkfact.batch;

import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import org.springframework.batch.item.ItemReader;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ItemReader thread-safe pour les configs de datasets batch.
 * Utilise une ConcurrentLinkedQueue pour la lecture parallèle.
 * Retourne null quand la queue est vide (signale la fin du flux à Spring Batch).
 */
public class BatchItemReader implements ItemReader<BatchDataSetConfigDTO> {

    private final Queue<BatchDataSetConfigDTO> queue;

    public BatchItemReader(List<BatchDataSetConfigDTO> configs) {
        this.queue = new ConcurrentLinkedQueue<>(configs);
    }

    @Override
    public BatchDataSetConfigDTO read() {
        return queue.poll(); // null = fin de l'input
    }
}
