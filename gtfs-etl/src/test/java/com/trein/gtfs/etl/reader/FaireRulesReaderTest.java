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
import com.trein.gtfs.vo.GtfsFareRule;

/**
 * Testing GTFS CSV file parsing for faire rules.
 * 
 * @author trein
 */
public class FaireRulesReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/fare_rules.txt";
    
    private CSVReader<GtfsFareRule> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsFareRule> parser = new CSVHeaderAwareEntryParser<GtfsFareRule>(GtfsFareRule.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsFareRule>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsFareRule> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsFareRule entity = this.reader.readAll().get(0);
	
	// fare_id,route_id,origin_id,destination_id,contains_id
	// p,AB,,,
	assertThat(entity, notNullValue());
	assertThat(entity.getFareId(), is("p"));
	assertThat(entity.getRouteId(), is("AB"));
	assertThat(entity.getOriginZoneId(), is(nullValue()));
	assertThat(entity.getDestinationZoneId(), is(nullValue()));
	assertThat(entity.getContainsId(), is(nullValue()));
    }
    
}
