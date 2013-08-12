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
import com.trein.gtfs.vo.GtfsFrequency;

/**
 * Testing GTFS CSV file parsing for frequencies.
 * 
 * @author trein
 */
public class FrequencyReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/frequencies.txt";
    
    private CSVReader<GtfsFrequency> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsFrequency> parser = new CSVHeaderAwareEntryParser<GtfsFrequency>(GtfsFrequency.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsFrequency>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsFrequency> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsFrequency entity = this.reader.readAll().get(0);
	
	// trip_id,start_time,end_time,headway_secs
	// STBA,6:00:00,22:00:00,1800
	assertThat(entity, notNullValue());
	assertThat(entity.getTripId(), is("STBA"));
	assertThat(entity.getStartTime(), is("6:00:00"));
	assertThat(entity.getEndTime(), is("22:00:00"));
	assertThat(entity.getHeadwaySecs(), is(Long.valueOf(1800)));
	assertThat(entity.getExactTime(), is(nullValue()));
    }
    
}
