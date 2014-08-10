package com.trein.gtfs.jpa.datasource;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JpaPropertiesResolver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaPropertiesResolver.class);
    
    private static final String ENV_SYSTEM = "environment";
    private static final String DEVELOPMENT = "development";
    private static final String AWS = "aws";
    
    private int minPoolSize;
    private int maxPoolSize;
    private int maxStatements;
    private int idleConnectionTestPeriod;
    
    private Database database;
    
    @PostConstruct
    protected void init() {
	if (isDevelopmentEnv()) {
	    LOGGER.info("Development environment detected. Using Local database");
	    this.database = new LocalDatabase();
	} else if (isAWSEnv()) {
	    LOGGER.info("AWS environment detected. Using Amazon WS RDS database");
	    this.database = new RDSDatabase();
	} else {
	    LOGGER.info("Using OpenShift database");
	    this.database = new OpenshiftDatabase();
	}
	
	this.minPoolSize = 5;
	this.maxPoolSize = 20;
	this.maxStatements = 50;
	this.idleConnectionTestPeriod = 3000;
    }
    
    private boolean isDevelopmentEnv() {
	return (getEnvName() != null) && getEnvName().equals(DEVELOPMENT);
    }
    
    private boolean isAWSEnv() {
	return (getEnvName() != null) && getEnvName().equals(AWS);
    }
    
    private String getEnvName() {
	return System.getProperty(ENV_SYSTEM);
    }
    
    public String getDiverClassName() {
	return this.database.getDriverClassName();
    }
    
    public String getJdbcUrl() {
	return this.database.getJdbcUrl();
    }
    
    public String getUsername() {
	return this.database.getUsername();
    }
    
    public String getPassword() {
	return this.database.getPassword();
    }
    
    public int getMinPoolSize() {
	return this.minPoolSize;
    }
    
    public int getMaxPoolSize() {
	return this.maxPoolSize;
    }
    
    public int getMaxStatements() {
	return this.maxStatements;
    }
    
    public int getIdleConnectionTestPeriod() {
	return this.idleConnectionTestPeriod;
    }
    
}
