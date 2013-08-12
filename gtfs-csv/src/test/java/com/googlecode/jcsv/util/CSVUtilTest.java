package com.googlecode.jcsv.util;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CSVUtilTest {

	@Test
	public void testImplode() {
		String[] data = {"A", "B", "C"};
		String result = CSVUtil.implode(data, ";");
		assertEquals(result, "A;B;C");

		data = new String[]{"A"};
		result = CSVUtil.implode(data, ";");
		assertEquals(result, "A");

		data = new String[0];
		result = CSVUtil.implode(data, ";");
		assertEquals(result, "");
	}

	@Test
	public void testSplit() {
		String[] expected = null;
		String[] result = CSVUtil.split(null, '*', true);
		assertArrayEquals(expected, result);

		expected = new String[0];
		result = CSVUtil.split("", '*', true);
		assertArrayEquals(expected, result);

		expected = new String[]{"a", "b", "c"};
		result = CSVUtil.split("a;b;c", ';', true);
		assertArrayEquals(expected, result);

		expected = new String[]{"a", "", "", "c"};
		result = CSVUtil.split("a;;;c", ';', true);
		assertArrayEquals(expected, result);

		expected = new String[]{"a", "c"};
		result = CSVUtil.split("a;;;c", ';', false);
		assertArrayEquals(expected, result);

		expected = new String[]{"", "a", "b", "c", ""};
		result = CSVUtil.split(";a;b;c;", ';', true);
		assertArrayEquals(expected, result);
	}

}
