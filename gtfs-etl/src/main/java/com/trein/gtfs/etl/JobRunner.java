package com.trein.gtfs.etl;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.trein.gtfs.etl.config.JpaEtlConfig;
import com.trein.gtfs.etl.config.MongoEtlConfig;

public class JobRunner {

    public static void main(String[] args) throws Exception {
        // launchJpaJob();
        launchMongoJob();
    }

    protected static void launchMongoJob() throws Exception {
        try (ConfigurableApplicationContext appContext = new AnnotationConfigApplicationContext(MongoEtlConfig.class)) {
            JobLauncher laucher = appContext.getBean(JobLauncher.class);
            Job initJob = appContext.getBean(MongoEtlConfig.JOB_NAME, Job.class);
            laucher.run(initJob, new JobParameters());
        }
    }

    protected static void launchJpaJob() throws Exception {
        try (ConfigurableApplicationContext appContext = new AnnotationConfigApplicationContext(JpaEtlConfig.class)) {
            JobLauncher laucher = appContext.getBean(JobLauncher.class);
            Job initJob = appContext.getBean(JpaEtlConfig.JOB_NAME, Job.class);
            laucher.run(initJob, new JobParameters());
        }
    }
}
