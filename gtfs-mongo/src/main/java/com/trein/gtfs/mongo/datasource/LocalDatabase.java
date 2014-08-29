package com.trein.gtfs.mongo.datasource;

public class LocalDatabase implements Database {

    private static final String DB_PASSWORD = "root";
    private static final String DB_USERNAME = "root";
    private static final String URL = "localhost";
    private static final String NAME = "gtfs";

    private final String name = NAME;
    private final String url = URL;
    private final String username = DB_USERNAME;
    private final String password = DB_PASSWORD;

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

}
