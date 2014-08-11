package com.trein.gtfs.etl.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.MapExecutionContextDao;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import com.trein.gtfs.etl.job.GtfsItem;
import com.trein.gtfs.etl.job.GtfsItemReader;
import com.trein.gtfs.etl.job.GtfsJpaItemWriter;
import com.trein.gtfs.etl.job.GtfsMongoItemWriter;
import com.trein.gtfs.mongo.MongoRepositoryConfig;

@Configuration
@EnableBatchProcessing
@Import({ MongoRepositoryConfig.class })
@ComponentScan(basePackages = { "com.trein.gtfs" })
public class MongoEtlConfig {

    private static final String GTFS_BASE_DIR = "br_poa/";
    public static final String STEP_NAME = "gtfs_unique_step";
    public static final String JOB_NAME = "gtfs_parser_job";
    private static final int CHUNK_SIZE = 1000;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
    
    @Bean
    public JobRepository jobRepository() throws Exception {
        return new SimpleJobRepository(new MapJobInstanceDao(), new MapJobExecutionDao(), new MapStepExecutionDao(),
                new MapExecutionContextDao());
    }
    
    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
    
    @Bean(name = JOB_NAME)
    public Job cvJob(Step step) {
        return this.jobs.get(JOB_NAME).flow(step).end().build();
    }

    @Bean
    public Step step(GtfsItemReader reader, GtfsJpaItemWriter writer) {
        return this.steps.get(STEP_NAME).<Object, GtfsItem> chunk(CHUNK_SIZE).reader(reader).writer(writer).build();
    }
    
    @Bean
    public GtfsMongoItemWriter writer() {
        return new GtfsMongoItemWriter();
    }

    @Bean
    public GtfsItemReader reader() {
        return new GtfsItemReader(GTFS_BASE_DIR);
    }
    
}
