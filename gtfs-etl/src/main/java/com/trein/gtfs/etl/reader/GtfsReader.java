package com.trein.gtfs.etl.reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.googlecode.jcsv.reader.CSVReader;
import com.trein.gtfs.csv.annotations.GtfsFile;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;
import com.trein.gtfs.csv.reader.CSVHeaderAwareReaderBuilder;

public class GtfsReader<T> {

    private final Class<T> entity;
    private final GtfsParserProvider<T> provider;

    public GtfsReader(Class<T> entity, GtfsParserProvider<T> provider) {
        this.entity = entity;
        this.provider = provider;
    }

    public List<T> read(String baseDir) throws IOException {
        String filename = this.entity.getAnnotation(GtfsFile.class).value();
        String path = baseDir + filename;
        Reader csv = new InputStreamReader(getClass().getResourceAsStream(path));
        CSVHeaderAwareEntryParser<T> entryParser = this.provider.get(this.entity);
        CSVReader<T> reader = new CSVHeaderAwareReaderBuilder<T>(csv).entryParser(entryParser).build();

        return reader.readAll();
    }

    static class GtfsReaderProvider {

        public <T> GtfsReader<T> get(Class<T> entity) {
            GtfsParserProvider<T> provider = new GtfsParserProvider<T>();

            return new GtfsReader<T>(entity, provider);
        }
    }
}
