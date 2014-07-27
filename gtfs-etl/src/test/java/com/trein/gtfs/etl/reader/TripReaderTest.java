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
import com.trein.gtfs.csv.vo.GtfsTrip;

/**
 * Testing GTFS CSV file parsing for trips.
 * 
 * @author trein
 */
public class TripReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/trips.txt";
    
    private CSVReader<GtfsTrip> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsTrip> parser = new CSVHeaderAwareEntryParser<GtfsTrip>(GtfsTrip.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsTrip>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsTrip> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsTrip entity = this.reader.readAll().get(0);
	
	// AB,FULLW,AB1,to Bullfrog,0,1,
	assertThat(entity, notNullValue());
	assertThat(entity.getId(), is("AB1"));
	assertThat(entity.getRouteId(), is("AB"));
	assertThat(entity.getServiceId(), is("FULLW"));
	assertThat(entity.getHeadsign(), is("to Bullfrog"));
	assertThat(entity.getDirectionType(), is(Integer.valueOf(0)));
	assertThat(entity.getBlockId(), is(Integer.valueOf(1)));
	assertThat(entity.getShapeId(), is(nullValue()));
	assertThat(entity.getShortName(), is(nullValue()));
	assertThat(entity.getWheelchairType(), is(nullValue()));
    }
    
}
