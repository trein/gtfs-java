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
import com.googlecode.jcsv.reader.internal.CSVHeaderAwareEntryParser;
import com.googlecode.jcsv.reader.internal.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.vo.GtfsTransfer;

/**
 * Testing GTFS CSV file parsing for transfers.
 * 
 * @author trein
 */
public class TransferReaderTest {
    
    private static final String EXPECTED_INPUT = "/sample/transfers.txt";
    
    private CSVReader<GtfsTransfer> reader;
    
    @Before
    public void setup() {
	Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
	ValueProcessorProvider vpp = new ValueProcessorProvider();
	CSVHeaderAwareEntryParser<GtfsTransfer> parser = new CSVHeaderAwareEntryParser<GtfsTransfer>(GtfsTransfer.class, vpp);
	
	this.reader = new CSVHeaderAwareReaderBuilder<GtfsTransfer>(csv).entryParser(parser).build();
    }
    
    @Test
    public void shouldParseValidEntity() throws IOException {
	List<GtfsTransfer> entities = this.reader.readAll();
	
	assertThat(entities, notNullValue());
    }
    
    @Test
    public void shouldParseEntityCorrectly() throws IOException {
	GtfsTransfer entity = this.reader.readAll().get(0);
	
	// from_stop_id,to_stop_id,transfer_type,min_transfer_time
	// S6,S7,2,300
	assertThat(entity, notNullValue());
	assertThat(entity.getFromStopId(), is("S6"));
	assertThat(entity.getToStopId(), is("S7"));
	assertThat(entity.getTransferType(), is(Integer.valueOf(2)));
	assertThat(entity.getMinTransferTimeSecs(), is(Long.valueOf(300)));
    }
    
}
