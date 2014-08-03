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
import com.trein.gtfs.csv.vo.GtfsFareAttribute;

/**
 * Testing GTFS CSV file parsing for fare attributes.
 *
 * @author trein
 */
public class FareAttributeReaderTest {

    private static final String EXPECTED_INPUT = "/sample/fare_attributes.txt";

    private CSVReader<GtfsFareAttribute> reader;

    @Before
    public void setup() {
        Reader csv = new InputStreamReader(getClass().getResourceAsStream(EXPECTED_INPUT));
        ValueProcessorProvider vpp = new ValueProcessorProvider();
        CSVHeaderAwareEntryParser<GtfsFareAttribute> parser = new CSVHeaderAwareEntryParser<GtfsFareAttribute>(
                GtfsFareAttribute.class, vpp);
        
        this.reader = new CSVHeaderAwareReaderBuilder<GtfsFareAttribute>(csv).entryParser(parser).build();
    }

    @Test
    public void shouldParseValidEntity() throws IOException {
        List<GtfsFareAttribute> entities = this.reader.readAll();
        
        assertThat(entities, notNullValue());
    }

    @Test
    public void shouldParseEntityCorrectly() throws IOException {
        GtfsFareAttribute entity = this.reader.readAll().get(0);
        
        // fare_id,price,currency_type,payment_method,transfers,transfer_duration
        // p,1.25,USD,0,0,
        assertThat(entity, notNullValue());
        assertThat(entity.getFareId(), is("p"));
        assertThat(entity.getPrice(), is(Double.valueOf(1.25)));
        assertThat(entity.getCurrencyType(), is("USD"));
        assertThat(entity.getPaymentType(), is(Integer.valueOf(0)));
        assertThat(entity.getTransfers(), is(String.valueOf(0)));
        assertThat(entity.getTransferDuration(), is(nullValue()));
    }

}
