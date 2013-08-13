package com.trein.gtfs.etl.reader;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class GtfsRepository {

    private final ListMultimap<Class<?>, Object> loadedEntities = ArrayListMultimap.create();

    public void addAll(Class<?> entity, List<? extends Object> data) {
        this.loadedEntities.putAll(entity, data);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getEntities(Class<T> entity) {
        return (List<T>) this.loadedEntities.get(entity);
    }

    public void removeAll() {
        this.loadedEntities.clear();
    }
}
