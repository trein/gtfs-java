package com.trein.gtfs.orm.datasource;

public interface Database {
    
    static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String JDBC_URL_PATTERN = "jdbc:mysql://%s:%s/%s";
    
    String getDriverClassName();
    
    String getJdbcUrl();
    
    String getUsername();
    
    String getPassword();
}
