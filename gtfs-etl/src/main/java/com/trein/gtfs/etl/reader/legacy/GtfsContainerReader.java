package com.trein.gtfs.etl.reader.legacy;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.csv.vo.GtfsCalendar;
import com.trein.gtfs.csv.vo.GtfsCalendarDate;
import com.trein.gtfs.csv.vo.GtfsRoute;
import com.trein.gtfs.csv.vo.GtfsShape;
import com.trein.gtfs.csv.vo.GtfsStop;
import com.trein.gtfs.csv.vo.GtfsStopTime;
import com.trein.gtfs.csv.vo.GtfsTransfer;
import com.trein.gtfs.csv.vo.GtfsTrip;

@Component
public class GtfsContainerReader {
    
    private static final Class<?>[] ENTITIES = { GtfsAgency.class, GtfsCalendarDate.class, GtfsCalendar.class, GtfsRoute.class,
            GtfsShape.class, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class, GtfsTrip.class };
    
    private final GtfsRepository repository;
    private final GtfsReaderProvider provider;
    
    @Autowired
    public GtfsContainerReader(GtfsReaderProvider provider, GtfsRepository repository) {
        this.provider = provider;
        this.repository = repository;
    }
    
    public void load(String baseDir) throws IOException {
        for (Class<?> entityClass : ENTITIES) {
            this.getRepository().addAll(entityClass, read(entityClass, baseDir));
        }
    }
    
    private <T> List<T> read(Class<T> entityClass, String baseDir) throws IOException {
        GtfsReader<T> reader = this.provider.get(entityClass);
        return reader.read(baseDir);
    }

    public GtfsRepository getRepository() {
        return repository;
    }
}
