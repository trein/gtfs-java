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
import com.trein.gtfs.csv.vo.GtfsShape;

/**
 * Testing GTFS CSV file parsing for shapes.
 * 
 * @author trein
 */
public class ShapeReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/shapes.txt";
    
    private CSVReader<GtfsShape> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsShape> parser = new CSVHeaderAwareEntryParser<GtfsShape>(GtfsShape.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsShape>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsShape> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsShape entity = this.reader.readAll().get(0);
	
	// shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled
	// A_shp,37.61956,-122.48161,1,0
	assertThat(entity, notNullValue());
	assertThat(entity.getId(), is("A_shp"));
	assertThat(entity.getLat(), is(Double.valueOf(37.61956)));
	assertThat(entity.getLng(), is(Double.valueOf(-122.48161)));
	assertThat(entity.getSequence(), is(Integer.valueOf(1)));
	assertThat(entity.getDistanceTraveled(), is(Double.valueOf(0)));
    }
    
}
