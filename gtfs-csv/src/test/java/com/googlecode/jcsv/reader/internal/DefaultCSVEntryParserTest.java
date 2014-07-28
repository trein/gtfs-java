package com.googlecode.jcsv.reader.internal;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.googlecode.jcsv.reader.CSVEntryParser;

public class DefaultCSVEntryParserTest {
    
    @Test
    public void testParseEntry() {
	CSVEntryParser<String[]> parser = new DefaultCSVEntryParser();
	String[] data = { "A", "B", "C" };
	List<String> contextData = Arrays.asList(data);
	
	assertArrayEquals(data, parser.parseEntry(new CSVIndexParsingContext(contextData)));
    }
    
}
