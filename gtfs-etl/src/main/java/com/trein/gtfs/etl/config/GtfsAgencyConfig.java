package com.trein.gtfs.etl.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.trein.gtfs.etl.mapper.AgencyFieldSetMapper;
import com.trein.gtfs.etl.reader.CSVItemReaderBuilder;
import com.trein.gtfs.etl.writer.JpaAgencyItemWriter;
import com.trein.gtfs.vo.Agency;

@Configuration
public class GtfsAgencyConfig {
    
    public static final String STEP_NAME = "step";
    public static final String JOB_NAME = "job";
    
    @Autowired
    private JobBuilderFactory jobs;
    
    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private JpaAgencyItemWriter writer;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Value("br_poa/agency.txt")
    private Resource resources;
    
    @Bean(name = JOB_NAME)
    public Job cvJob() {
	return this.jobs.get(JOB_NAME).flow(step()).end().build();
    }
    
    @Bean
    public Step step() {
	return this.steps.get(STEP_NAME).<Object, Agency> chunk(100).reader(reader()).writer(this.writer).transactionManager(
	        this.transactionManager).build();
    }
    
    public MultiResourceItemReader<Agency> reader() {
	return new CSVItemReaderBuilder<Agency>(new AgencyFieldSetMapper(), this.resources).build();
    }
}
