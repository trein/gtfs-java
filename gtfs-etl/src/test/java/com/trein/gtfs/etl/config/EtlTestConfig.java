package com.trein.gtfs.etl.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.MapExecutionContextDao;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import com.trein.gtfs.jpa.JpaRepositoryConfig;

@Configuration
@EnableBatchProcessing
@Import(JpaRepositoryConfig.class)
@ComponentScan(basePackages = { "com.trein.etl" })
public class EtlTestConfig {
    
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
    
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
	return new JobLauncherTestUtils();
    }
    
}
