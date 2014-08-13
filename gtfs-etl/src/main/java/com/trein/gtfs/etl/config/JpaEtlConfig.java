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
import com.trein.gtfs.etl.job.GtfsJpaItemWriter;
import com.trein.gtfs.jpa.JpaRepositoryConfig;

@Configuration
@Import({ JpaRepositoryConfig.class, EtlConfig.class })
public class JpaEtlConfig {
    
    private static final String GTFS_BASE_DIR = "br_poa/";
    public static final String STEP_NAME = "jpa_unique_step";
    public static final String JOB_NAME = "jpa_parser_job";
    private static final int CHUNK_SIZE = 1000;
    
    @Autowired
    private JobBuilderFactory jobs;
    
    @Autowired
    private StepBuilderFactory steps;
    
    @Bean(name = JOB_NAME)
    public Job jpaJob(GtfsItemReader reader, GtfsJpaItemWriter writer) {
        return this.jobs.get(JOB_NAME).flow(jpaStep(reader, writer)).end().build();
    }
    
    @Bean
    public Step jpaStep(GtfsItemReader reader, GtfsJpaItemWriter writer) {
        return this.steps.get(STEP_NAME).<Object, GtfsItem> chunk(CHUNK_SIZE).reader(reader).writer(writer).build();
    }

    @Bean
    public GtfsJpaItemWriter jpaWriter() {
        return new GtfsJpaItemWriter();
    }
    
    @Bean
    public GtfsItemReader reader() {
        return new GtfsItemReader(GTFS_BASE_DIR);
    }

}
