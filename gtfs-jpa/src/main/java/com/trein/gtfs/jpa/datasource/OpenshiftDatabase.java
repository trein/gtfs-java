package com.trein.gtfs.jpa.datasource;

public class OpenshiftDatabase implements Database {
    
    private static final String DB_PASSWORD_ENV_VAR = "OPENSHIFT_MYSQL_DB_PASSWORD";
    private static final String DB_USERNAME_ENV_VAR = "OPENSHIFT_MYSQL_DB_USERNAME";
    private static final String DB_PORT_ENV_VAR = "OPENSHIFT_MYSQL_DB_PORT";
    private static final String DB_HOST_ENV_VAR = "OPENSHIFT_MYSQL_DB_HOST";
    private static final String DB_NAME_ENV_VAR = "OPENSHIFT_APP_NAME";
    
    private final String driverClassName;
    private final String jdbcUrl;
    private final String name;
    private final String username;
    private final String password;
    
    public OpenshiftDatabase() {
        this.driverClassName = JDBC_DRIVER;
        this.username = getEnv(DB_USERNAME_ENV_VAR);
        this.password = getEnv(DB_PASSWORD_ENV_VAR);
        this.name = getEnv(DB_NAME_ENV_VAR);
        this.jdbcUrl = assembleOpenshiftUrl(this.name);
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

    private String assembleOpenshiftUrl(String name) {
        return String.format(JDBC_URL_PATTERN, getEnv(DB_HOST_ENV_VAR), getEnv(DB_PORT_ENV_VAR), name);
    }
    
    private String getEnv(String key) {
        return System.getenv(key);
    }
    
}
