package com.trein.gtfs.etl.job;

public class GtfsItem {

    private final Class<Object> entityClass;
    private final Object entity;

    public GtfsItem(Class<Object> entityClass, Object parsedEntity) {
        this.entityClass = entityClass;
        this.entity = parsedEntity;
    }

    public Class<Object> getEntityClass() {
        return this.entityClass;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getEntity() {
        return (T) this.entity;
    }
    
    @Override
    public String toString() {
        return String.format("%s::%s", this.entity, this.entityClass.getCanonicalName());
    }
}
