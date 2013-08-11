package com.trein.gtfs.orm.datasource;

public class LocalDatabse implements Database {
    
    private static final String DB_PASSWORD = "root";
    private static final String DB_USERNAME = "root";
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/gtfs";
    
    private final String driverClassName;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    
    public LocalDatabse() {
	this.driverClassName = JDBC_DRIVER;
	this.jdbcUrl = JDBC_URL;
	this.username = DB_USERNAME;
	this.password = DB_PASSWORD;
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
    
}
