package com.trein.gtfs.etl.reader.legacy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVReader;
import com.trein.gtfs.csv.annotations.GtfsFile;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;
import com.trein.gtfs.csv.reader.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.csv.vo.GtfsCalendar;
import com.trein.gtfs.csv.vo.GtfsCalendarDate;
import com.trein.gtfs.csv.vo.GtfsRoute;
import com.trein.gtfs.csv.vo.GtfsShape;
import com.trein.gtfs.csv.vo.GtfsStop;
import com.trein.gtfs.csv.vo.GtfsStopTime;
import com.trein.gtfs.csv.vo.GtfsTransfer;
import com.trein.gtfs.csv.vo.GtfsTrip;

public class GtfsReader<T> {
    
    private static final List<Class<?>> ENTITIES = Arrays.asList(GtfsAgency.class, GtfsCalendarDate.class, GtfsCalendar.class,
            GtfsRoute.class, GtfsShape.class, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class, GtfsTrip.class);
    
    private final Iterator<Class<?>> entityIterator;

    public GtfsReader() {
        this.entityIterator = ENTITIES.iterator();
    }

    public T read(String baseDir) throws IOException {
        for (Class<?> entityClass : ENTITIES) {
            String filename = this.entity.getAnnotation(GtfsFile.class).value();
            String path = baseDir + filename;
            Reader csv = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path));
            
            ValueProcessorProvider processor = new ValueProcessorProvider();
            CSVHeaderAwareEntryParser<T> entryParser = new CSVHeaderAwareEntryParser<T>(this.entity, processor);
            CSVReader<T> reader = new CSVHeaderAwareReaderBuilder<T>(csv).entryParser(entryParser).build();
            
            return reader.readNext();
        }
    }

}
