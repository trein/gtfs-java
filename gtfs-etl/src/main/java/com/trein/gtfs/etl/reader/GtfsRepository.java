package com.trein.gtfs.etl.reader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GtfsRepository {
    
    private final Map<Class<?>, Collection<?>> loaded = new HashMap<Class<?>, Collection<?>>();
    
    public void addAll(Class<?> entity, Collection<?> data) {
	this.loaded.put(entity, data);
    }
}
