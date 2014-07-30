package com.trein.gtfs.etl.job;

public class GtfsItem {

    public final Class<Object> entityClass;
    public final Object entity;

    public GtfsItem(Class<Object> entityClass, Object parsedEntity) {
        this.entityClass = entityClass;
        this.entity = parsedEntity;
    }
    
    @Override
    public String toString() {
        return String.format("%s::%s", this.entity, this.entityClass.getCanonicalName());
    }
}
