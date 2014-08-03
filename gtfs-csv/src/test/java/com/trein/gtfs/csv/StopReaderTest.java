package com.trein.gtfs.csv;

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
import com.trein.gtfs.csv.vo.GtfsStop;

/**
 * Testing GTFS CSV file parsing for stops.
 * 
 * @author trein
 */
public class StopReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/stops.txt";
    
    private CSVReader<GtfsStop> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsStop> parser = new CSVHeaderAwareEntryParser<GtfsStop>(GtfsStop.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsStop>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsStop> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsStop entity = this.reader.readAll().get(0);
	
	// FUR_CREEK_RES,Furnace Creek Resort (Demo),,36.425288,-117.133162,,
	assertThat(entity, notNullValue());
	assertThat(entity.getId(), is("FUR_CREEK_RES"));
	assertThat(entity.getName(), is("Furnace Creek Resort (Demo)"));
	assertThat(entity.getDesc(), is(nullValue()));
	assertThat(entity.getUrl(), is(nullValue()));
	assertThat(entity.getZoneId(), is(nullValue()));
	assertThat(entity.getWheelchairType(), is(nullValue()));
	assertThat(entity.getTimezone(), is(nullValue()));
	assertThat(entity.getLat(), is(Double.valueOf(36.425288)));
	assertThat(entity.getLng(), is(Double.valueOf(-117.133162)));
    }
    
}
