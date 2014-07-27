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
import com.trein.gtfs.csv.vo.GtfsRoute;

/**
 * Testing GTFS CSV file parsing for routes.
 * 
 * @author trein
 */
public class RouteReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/routes.txt";
    
    private CSVReader<GtfsRoute> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsRoute> parser = new CSVHeaderAwareEntryParser<GtfsRoute>(GtfsRoute.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsRoute>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsRoute> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsRoute entity = this.reader.readAll().get(0);
	
	// AB,DTA,10,Airport - Bullfrog,,3,,,
	assertThat(entity, notNullValue());
	assertThat(entity.getId(), is("AB"));
	assertThat(entity.getAgencyId(), is("DTA"));
	assertThat(entity.getShortName(), is("10"));
	assertThat(entity.getLongName(), is("Airport - Bullfrog"));
	assertThat(entity.getRouteType(), is(Integer.valueOf(3)));
	assertThat(entity.getDesc(), is(nullValue()));
	assertThat(entity.getUrl(), is(nullValue()));
	assertThat(entity.getHexTextColor(), is(nullValue()));
	assertThat(entity.getHexPathColor(), is(nullValue()));
    }
    
}
