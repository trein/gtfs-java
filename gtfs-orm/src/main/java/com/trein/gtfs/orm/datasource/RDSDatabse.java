package com.trein.gtfs.orm.datasource;

public class RDSDatabse implements Database {
    
    private static final String DB_USERNAME_VAR = "PARAM1";
    private static final String DB_PASSWORD_VAR = "PARAM2";
    private static final String JDBC_URL_VAR = "JDBC_CONNECTION_STRING";
    
    private final String driverClassName;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    
    public RDSDatabse() {
	this.driverClassName = JDBC_DRIVER;
	this.jdbcUrl = getProperty(JDBC_URL_VAR);
	this.username = getProperty(DB_USERNAME_VAR);
	this.password = getProperty(DB_PASSWORD_VAR);
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
    
    private String getProperty(String key) {
	return System.getProperty(key);
    }
    
}
