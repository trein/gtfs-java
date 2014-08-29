package com.trein.gtfs.mongo.datasource;

public interface Database {

    String getDbName();

    String getUrl();

    String getUsername();

    String getPassword();
}
