package com.trein.gtfs.orm.datasource;

public class OpenshiftDatabse implements Database {
    
    private static final String DB_PASSWORD_ENV_VAR = "OPENSHIFT_DB_PASSWORD";
    private static final String DB_USERNAME_ENV_VAR = "OPENSHIFT_DB_USERNAME";
    private static final String DB_PORT_ENV_VAR = "OPENSHIFT_DB_PORT";
    private static final String DB_HOST_ENV_VAR = "OPENSHIFT_DB_HOST";
    private static final String DB_NAME = "gtfs";
    
    private final String driverClassName;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    
    public OpenshiftDatabse() {
	this.driverClassName = JDBC_DRIVER;
	this.jdbcUrl = assembleOpenshiftUrl();
	this.username = getEnv(DB_USERNAME_ENV_VAR);
	this.password = getEnv(DB_PASSWORD_ENV_VAR);
    }
    
    @Override
    public String getDriverClassName() {
	return this.driverClassName;
    }
    
    @Override
    public String getJdbcUrl() {
	return this.jdbcUrl;
    }
    
    @Override
    public String getUsername() {
	return this.username;
    }
    
    @Override
    public String getPassword() {
	return this.password;
    }
    
    private String assembleOpenshiftUrl() {
	return String.format(JDBC_URL_PATTERN, getEnv(DB_HOST_ENV_VAR), getEnv(DB_PORT_ENV_VAR), DB_NAME);
    }
    
    private String getEnv(String key) {
	return System.getenv(key);
    }
    
}
