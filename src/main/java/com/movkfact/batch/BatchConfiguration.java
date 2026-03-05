package com.movkfact.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import com.movkfact.service.DataGeneratorService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

/**
 * Configuration Spring Batch 5 pour la génération de datasets en parallèle.
 *
 * Architecture :
 * - Job "batchGenerationJob" avec un Step "dataGenerationStep"
 * - chunk(1) : chaque dataset config = 1 chunk = 1 transaction
 * - ThreadPoolTaskExecutor (4 workers) : exécution parallèle des chunks
 * - faultTolerant + retry(3) + skip : echec d'un dataset n'arrête pas les autres
 * - Backoff exponentiel : 100ms → 500ms → 1000ms (max)
 *
 * NOTE: @EnableBatchProcessing n'est PAS utilisé — désactive l'auto-config en Spring Batch 5.
 */
@Configuration
public class BatchConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private BatchJobConfigStore configStore;

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BatchJobExecutionListener batchJobExecutionListener;

    // ============================================================================
    // ThreadPoolTaskExecutor — 4 workers parallèles (max 8)
    // ============================================================================

    @Bean("batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-worker-");
        executor.initialize();
        return executor;
    }

    // ============================================================================
    // Async JobLauncher — retourne immédiatement avec jobId
    // ============================================================================

    @Bean("asyncJobLauncher")
    public TaskExecutorJobLauncher asyncJobLauncher() throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }

    // ============================================================================
    // Step-scoped ItemReader — thread-safe via ConcurrentLinkedQueue
    // ============================================================================

    @Bean
    @StepScope
    public BatchItemReader configItemReader(
            @Value("#{jobParameters['configKey']}") String configKey) {
        List<BatchDataSetConfigDTO> configs = configStore.retrieveConfigs(configKey);
        return new BatchItemReader(configs);
    }

    // ============================================================================
    // Step-scoped Processor — stateless, thread-safe
    // ============================================================================

    @Bean
    @StepScope
    public DataSetItemProcessor dataSetItemProcessor() {
        return new DataSetItemProcessor(dataGeneratorService, domainRepository, objectMapper);
    }

    // ============================================================================
    // Step-scoped Writer
    // ============================================================================

    @Bean
    @StepScope
    public DataSetItemWriter dataSetItemWriter() {
        return new DataSetItemWriter(dataSetRepository, eventPublisher);
    }

    // ============================================================================
    // Backoff exponentiel : 100ms → 500ms → 1000ms (max)
    // ============================================================================

    @Bean
    public ExponentialBackOffPolicy batchBackOffPolicy() {
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(100);
        policy.setMultiplier(5.0);
        policy.setMaxInterval(1000);
        return policy;
    }

    // ============================================================================
    // Step : chunk(1) + parallel + retry + skip (graceful failure)
    // ============================================================================

    @Bean
    public Step dataGenerationStep(
            @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("dataGenerationStep", jobRepository)
            .<BatchDataSetConfigDTO, DataSet>chunk(1, transactionManager)
            .reader(configItemReader(null))       // null → Spring injecte via @StepScope proxy
            .processor(dataSetItemProcessor())
            .writer(dataSetItemWriter())
            .taskExecutor(taskExecutor)
            .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .noRetry(IllegalArgumentException.class)  // Domain not found → skip direct
                .backOffPolicy(batchBackOffPolicy())
                .skipLimit(Integer.MAX_VALUE)             // Graceful failure : skip on all retries exhausted
                .skip(Exception.class)
                .noSkip(IllegalArgumentException.class)   // Bad config → log but don't skip silently
                .listener((org.springframework.batch.core.ChunkListener) batchJobExecutionListener)
                .listener((org.springframework.batch.core.SkipListener<BatchDataSetConfigDTO, DataSet>) batchJobExecutionListener)
            .build();
    }

    // ============================================================================
    // Job
    // ============================================================================

    @Bean("batchGenerationJob")
    public Job batchGenerationJob(
            @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new JobBuilder("batchGenerationJob", jobRepository)
            .listener((org.springframework.batch.core.JobExecutionListener) batchJobExecutionListener)
            .start(dataGenerationStep(taskExecutor))
            .build();
    }
}
