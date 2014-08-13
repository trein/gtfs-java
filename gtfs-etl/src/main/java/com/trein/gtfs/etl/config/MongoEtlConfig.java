package com.trein.gtfs.etl.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.trein.gtfs.etl.job.GtfsItem;
import com.trein.gtfs.etl.job.GtfsItemReader;
import com.trein.gtfs.etl.job.GtfsMongoItemWriter;
import com.trein.gtfs.mongo.MongoRepositoryConfig;

@Configuration
@Import({ MongoRepositoryConfig.class, EtlConfig.class })
public class MongoEtlConfig {

    private static final String GTFS_BASE_DIR = "br_poa/";
    public static final String STEP_NAME = "mongo_unique_step";
    public static final String JOB_NAME = "mongo_parser_job";
    private static final int CHUNK_SIZE = 1000;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean(name = JOB_NAME)
    public Job mongoJob(GtfsItemReader reader, GtfsMongoItemWriter writer) {
        return this.jobs.get(JOB_NAME).flow(mongoStep(reader, writer)).end().build();
    }

    @Bean
    public Step mongoStep(GtfsItemReader reader, GtfsMongoItemWriter writer) {
        return this.steps.get(STEP_NAME).<Object, GtfsItem> chunk(CHUNK_SIZE).reader(reader).writer(writer).build();
    }
    
    @Bean
    public GtfsMongoItemWriter mongoWriter() {
        return new GtfsMongoItemWriter();
    }

    @Bean
    public GtfsItemReader mongoReader() {
        return new GtfsItemReader(GTFS_BASE_DIR);
    }
    
}
