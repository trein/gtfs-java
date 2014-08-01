package com.trein.gtfs.etl.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.trein.gtfs.etl.job.GtfsItem;
import com.trein.gtfs.etl.job.GtfsItemReader;
import com.trein.gtfs.etl.job.GtfsItemWriter;

@Configuration
public class GtfsJobConfig {
    
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
    
    @Bean(name = JOB_NAME)
    public Job cvJob(Step step) {
        return this.jobs.get(JOB_NAME).flow(step).end().build();
    }
    
    @Bean
    public Step step(GtfsItemReader reader, GtfsItemWriter writer) {
        return this.steps.get(STEP_NAME).<Object, GtfsItem> chunk(CHUNK_SIZE).reader(reader).writer(writer).build();
    }

    @Bean
    public GtfsItemWriter writer() {
        return new GtfsItemWriter();
    }
    
    @Bean
    public GtfsItemReader reader() {
        return new GtfsItemReader(GTFS_BASE_DIR);
    }
}
