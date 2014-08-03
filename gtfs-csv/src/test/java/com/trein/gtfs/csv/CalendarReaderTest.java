package com.trein.gtfs.csv;

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
import com.trein.gtfs.csv.vo.GtfsCalendar;

/**
 * Testing GTFS CSV file parsing for calendars.
 * 
 * @author trein
 */
public class CalendarReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/calendar.txt";
    
    private CSVReader<GtfsCalendar> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsCalendar> parser = new CSVHeaderAwareEntryParser<GtfsCalendar>(GtfsCalendar.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsCalendar>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsCalendar> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsCalendar entity = this.reader.readAll().get(0);
	
	// FULLW,1,1,1,1,1,1,1,20070101,20101231
	assertThat(entity, notNullValue());
	assertThat(entity.getServiceId(), is("FULLW"));
	assertThat(entity.getServiceId(), is("FULLW"));
	assertThat(entity.getMonday(), is(Integer.valueOf(1)));
	assertThat(entity.getTuesday(), is(Integer.valueOf(1)));
	assertThat(entity.getWednesday(), is(Integer.valueOf(1)));
	assertThat(entity.getThursday(), is(Integer.valueOf(1)));
	assertThat(entity.getFriday(), is(Integer.valueOf(1)));
	assertThat(entity.getStartDate(), is("20070101"));
	assertThat(entity.getEndDate(), is("20101231"));
    }
    
}
