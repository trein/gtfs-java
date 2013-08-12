package com.trein.gtfs.etl.reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.trein.gtfs.csv.annotations.GtfsFile;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;
import com.trein.gtfs.csv.reader.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.vo.GtfsAgency;
import com.trein.gtfs.vo.GtfsCalendar;
import com.trein.gtfs.vo.GtfsCalendarDate;
import com.trein.gtfs.vo.GtfsRoute;
import com.trein.gtfs.vo.GtfsShape;
import com.trein.gtfs.vo.GtfsStop;
import com.trein.gtfs.vo.GtfsStopTime;
import com.trein.gtfs.vo.GtfsTransfer;
import com.trein.gtfs.vo.GtfsTrip;

public class GtfsReader {
    
    private static final Class<?>[] ENTITIES = { GtfsAgency.class, GtfsCalendarDate.class, GtfsCalendar.class, GtfsRoute.class,
	    GtfsShape.class, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class, GtfsTrip.class };
    
    private final GtfsRepository repository = new GtfsRepository();
    
    public void load(String baseDir) throws IOException {
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	
	for (Class<?> entityClass : ENTITIES) {
	    this.repository.addAll(entityClass, read(baseDir, vpp, entityClass));
	}
    }
    
    private <T> List<T> read(String baseDir, ValueProcessorProvider vpp, Class<T> entityClass) throws IOException {
	String filename = entityClass.getAnnotation(GtfsFile.class).value();
	String path = baseDir + filename;
	
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(path));
	CSVEntryParser<T> parser = new CSVHeaderAwareEntryParser<T>(entityClass, vpp);
	CSVReader<T> reader = new CSVHeaderAwareReaderBuilder<T>(csv).entryParser(parser).build();
	
	return reader.readAll();
    }
    
}
