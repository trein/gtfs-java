package com.trein.gtfs.etl.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
import com.trein.gtfs.vo.GtfsCalendarDate;

/**
 * Testing GTFS CSV file parsing for stops.
 * 
 * @author trein
 */
public class CalendarDateReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/calendar_dates.txt";
    
    private CSVReader<GtfsCalendarDate> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsCalendarDate> parser = new CSVHeaderAwareEntryParser<GtfsCalendarDate>(
	        GtfsCalendarDate.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsCalendarDate>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsCalendarDate> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsCalendarDate entity = this.reader.readAll().get(0);
	
	// FULLW,20070604,2
	assertThat(entity, notNullValue());
	assertThat(entity.getServiceId(), is("FULLW"));
	assertThat(entity.getDate(), is("20070604"));
	assertThat(entity.getExceptionType(), is(Integer.valueOf(2)));
    }
    
}
