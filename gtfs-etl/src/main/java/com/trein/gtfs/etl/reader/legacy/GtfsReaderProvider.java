package com.trein.gtfs.etl.reader.legacy;

import org.springframework.stereotype.Component;

@Component
public class GtfsReaderProvider {

    public <T> GtfsReader<T> get(Class<T> entity) {
        GtfsParserProvider<T> provider = new GtfsParserProvider<T>();
        return new GtfsReader<T>(entity, provider);
    }
}
