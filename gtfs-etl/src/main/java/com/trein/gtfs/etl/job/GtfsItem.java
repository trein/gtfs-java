package com.trein.gtfs.etl.job;

public class GtfsItem {

    public final Class<Object> currentEntityClass;
    public final Object parsedEntity;

    public GtfsItem(Class<Object> currentEntityClass, Object parsedEntity) {
        this.currentEntityClass = currentEntityClass;
        this.parsedEntity = parsedEntity;
    }
    
}
