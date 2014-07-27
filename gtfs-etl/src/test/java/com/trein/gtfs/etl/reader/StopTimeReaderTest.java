package com.trein.gtfs.etl.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVReader;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;
import com.trein.gtfs.csv.reader.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.csv.vo.GtfsStopTime;

/**
 * Testing GTFS CSV file parsing for stop times.
 * 
 * @author trein
 */
public class StopTimeReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/stop_times.txt";
    
    private CSVReader<GtfsStopTime> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsStopTime> parser = new CSVHeaderAwareEntryParser<GtfsStopTime>(GtfsStopTime.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsStopTime>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsStopTime> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsStopTime entity = this.reader.readAll().get(0);
	
	// STBA,6:00:00,6:00:00,STAGECOACH,1,,,,
	assertThat(entity, notNullValue());
	assertThat(entity.getTripId(), is("STBA"));
	assertThat(entity.getArrivalTime(), is("6:00:00"));
	assertThat(entity.getDepartureTime(), is("6:00:00"));
	assertThat(entity.getStopId(), is("STAGECOACH"));
	assertThat(entity.getStopSequence(), is(Integer.valueOf(1)));
	assertThat(entity.getDropoffType(), is(nullValue()));
	assertThat(entity.getPickupType(), is(nullValue()));
	assertThat(entity.getShapeDistanceTraveled(), is(nullValue()));
	assertThat(entity.getStopHeadsign(), is(nullValue()));
    }
    
}
