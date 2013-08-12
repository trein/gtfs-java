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
import com.trein.gtfs.etl.writer.LogAgencyItemWriter;
import com.trein.gtfs.vo.GtfsAgency;

@Configuration
public class AgencySampleJobConfig {
    
    public static final String STEP_NAME = "step";
    public static final String JOB_NAME = "job";
    
    @Autowired
    private JobBuilderFactory jobs;
    
    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Value("sample/agency.txt")
    private Resource resources;
    
    @Bean(name = JOB_NAME)
    public Job cvJob() {
	return this.jobs.get(JOB_NAME).flow(step()).end().build();
    }
    
    @Bean
    public Step step() {
	return this.steps.get(STEP_NAME).<Object, GtfsAgency> chunk(100).reader(reader()).writer(writer()).transactionManager(
	        this.transactionManager).build();
    }
    
    @Bean
    public LogAgencyItemWriter writer() {
	return new LogAgencyItemWriter();
    }
    
    public MultiResourceItemReader<GtfsAgency> reader() {
	return new CSVItemReaderBuilder<GtfsAgency>(new AgencyFieldSetMapper(), this.resources).build();
    }
}
