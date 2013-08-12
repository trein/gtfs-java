package com.trein.gtfs.etl.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trein.gtfs.etl.config.AgencySampleJobConfig;
import com.trein.gtfs.etl.config.EtlTestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { EtlTestConfig.class, AgencySampleJobConfig.class })
public class AgencyJobIT {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Test
    public void testJob() throws Exception {
	JobExecution jobExecution = this.jobLauncherTestUtils.launchJob();
	
	assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
