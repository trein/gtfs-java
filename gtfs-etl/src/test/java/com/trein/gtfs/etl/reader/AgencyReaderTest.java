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
import com.googlecode.jcsv.reader.internal.CSVHeaderAwareEntryParser;
import com.googlecode.jcsv.reader.internal.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.vo.GtfsAgency;

/**
 * Testing GTFS CSV file parsing for agencies.
 * 
 * @author trein
 */
public class AgencyReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/agency.txt";
    
    private CSVReader<GtfsAgency> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsAgency> parser = new CSVHeaderAwareEntryParser<GtfsAgency>(GtfsAgency.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsAgency>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsAgency> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsAgency entity = this.reader.readAll().get(0);
	
	// DTA,Demo Transit Authority,http://google.com,America/Los_Angeles
	assertThat(entity, notNullValue());
	assertThat(entity.getId(), is("DTA"));
	assertThat(entity.getName(), is("Demo Transit Authority"));
	assertThat(entity.getUrl(), is("http://google.com"));
	assertThat(entity.getLang(), is(nullValue()));
	assertThat(entity.getPhone(), is(nullValue()));
	assertThat(entity.getTimezone(), is("America/Los_Angeles"));
    }
    
}
