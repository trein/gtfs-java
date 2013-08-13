package com.trein.gtfs.etl.reader;

import java.io.IOException;
import java.util.List;

import com.trein.gtfs.etl.reader.GtfsReader.GtfsReaderProvider;
import com.trein.gtfs.vo.GtfsAgency;
import com.trein.gtfs.vo.GtfsCalendar;
import com.trein.gtfs.vo.GtfsCalendarDate;
import com.trein.gtfs.vo.GtfsRoute;
import com.trein.gtfs.vo.GtfsShape;
import com.trein.gtfs.vo.GtfsStop;
import com.trein.gtfs.vo.GtfsStopTime;
import com.trein.gtfs.vo.GtfsTransfer;
import com.trein.gtfs.vo.GtfsTrip;

public class GtfsContainerReader {

    private static final Class<?>[] ENTITIES = {GtfsAgency.class, GtfsCalendarDate.class, GtfsCalendar.class, GtfsRoute.class,
        GtfsShape.class, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class, GtfsTrip.class};

    private final GtfsRepository repository;
    private final GtfsReaderProvider provider;

    public GtfsContainerReader(GtfsReaderProvider provider, GtfsRepository repository) {
        this.provider = provider;
        this.repository = repository;
    }

    public void load(String baseDir) throws IOException {
        for (Class<?> entityClass : ENTITIES) {
            this.repository.addAll(entityClass, read(entityClass, baseDir));
        }
    }

    private <T> List<T> read(Class<T> entityClass, String baseDir) throws IOException {
        GtfsReader<T> reader = this.provider.get(entityClass);

        return reader.read(baseDir);
    }
}
