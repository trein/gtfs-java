package com.trein.gtfs.etl.reader;

import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;

public class GtfsParserProvider<T> {

    private final ValueProcessorProvider processor;

    public GtfsParserProvider() {
        this.processor = new ValueProcessorProvider();
    }

    public CSVHeaderAwareEntryParser<T> get(Class<T> entity) {
        return new CSVHeaderAwareEntryParser<T>(entity, this.processor);
    }

}
