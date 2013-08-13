package com.trein.gtfs.etl.reader;

import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class GtfsRepository {

    private final Multimap<Class<?>, Object> loadedEntities = ArrayListMultimap.create();

    public void addAll(Class<?> entity, Collection<? extends Object> data) {
        this.loadedEntities.putAll(entity, data);
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> getEntities(Class<T> entity) {
        return (Collection<T>) this.loadedEntities.get(entity);
    }

    public void removeAll() {
        this.loadedEntities.clear();
    }
}
