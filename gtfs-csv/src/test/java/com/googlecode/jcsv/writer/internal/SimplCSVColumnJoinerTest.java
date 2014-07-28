package com.googlecode.jcsv.writer.internal;

import junit.framework.TestCase;

import org.junit.Test;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVColumnJoiner;

public class SimplCSVColumnJoinerTest extends TestCase {

	@Test
	public void testJoinColumns() {
		CSVColumnJoiner columnJoiner = new SimpleCSVColumnJoiner();

		// uses ; as delimiter
		final CSVStrategy strategy = CSVStrategy.DEFAULT;

		String[] data = {"A", "B", "C"};
		String result = columnJoiner.joinColumns(data, strategy);
		String expected = "A;B;C";
		assertEquals(expected, result);

		data = new String[]{"A"};
		result = columnJoiner.joinColumns(data, strategy);
		expected = "A";
		assertEquals(expected, result);

		data = new String[0];
		result = columnJoiner.joinColumns(data, strategy);
		expected = "";
		assertEquals(expected, result);
	}

}
