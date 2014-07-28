package com.googlecode.jcsv.writer.internal;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.googlecode.jcsv.writer.CSVEntryConverter;

public class DefaultCSVEntryConverterTest {

	@Test
	public void testConvertEntry() {
		CSVEntryConverter<String[]> converter = new DefaultCSVEntryConverter();
		String[] data = {"A", "B", "C"};

		assertArrayEquals(data, converter.convertEntry(data));
	}

}
