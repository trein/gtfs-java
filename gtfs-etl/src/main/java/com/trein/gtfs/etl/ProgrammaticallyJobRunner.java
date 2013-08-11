package com.trein.gtfs.etl;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.trein.gtfs.etl.config.EtlConfig;
import com.trein.gtfs.etl.config.GtfsAgencyConfig;

public class ProgrammaticallyJobRunner {
    
    public static void main(String[] args) throws Exception {
	ApplicationContext appContext = new AnnotationConfigApplicationContext(EtlConfig.class);
	JobLauncher laucher = appContext.getBean(JobLauncher.class);
	Job initJob = appContext.getBean(GtfsAgencyConfig.JOB_NAME, Job.class);
	
	laucher.run(initJob, new JobParameters());
    }
}
