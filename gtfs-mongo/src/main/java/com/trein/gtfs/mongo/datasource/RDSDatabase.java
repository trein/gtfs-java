package com.trein.gtfs.mongo.datasource;

public class RDSDatabase implements Database {

    private static final String NAME = "gtfs";
    private static final String DB_USERNAME_VAR = "PARAM1";
    private static final String DB_PASSWORD_VAR = "PARAM2";
    private static final String JDBC_URL_VAR = "JDBC_CONNECTION_STRING";

    private final String name;
    private final String url;
    private final String username;
    private final String password;

    public RDSDatabase() {
        this.name = NAME;
        this.url = getProperty(JDBC_URL_VAR);
        this.username = getProperty(DB_USERNAME_VAR);
        this.password = getProperty(DB_PASSWORD_VAR);
    }

    @Override
    public String getDbName() {
        return this.name;
    }

    @Override
    public String getUrl() {
        return this.url;
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
