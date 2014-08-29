package com.trein.gtfs.mongo.datasource;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MongoPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoPropertiesResolver.class);

    private static final String ENV_SYSTEM = "environment";
    private static final String DEVELOPMENT = "development";
    private static final String AWS = "aws";

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

    public String getDbName() {
        return this.database.getDbName();
    }

    public String getUrl() {
        return this.database.getUrl();
    }

    public String getUsername() {
        return this.database.getUsername();
    }

    public String getPassword() {
        return this.database.getPassword();
    }

}
