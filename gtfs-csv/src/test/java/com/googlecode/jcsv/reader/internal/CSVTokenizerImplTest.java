package com.googlecode.jcsv.reader.internal;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVTokenizer;

public class CSVTokenizerImplTest {
    
    @Test
    @SuppressWarnings("serial")
    public void testTokenizeLine() throws IOException {
	CSVTokenizer tokenizer = new CSVTokenizerImpl();
	
	// uses ; as delimiter
	final CSVStrategy strategy = CSVStrategy.DEFAULT;
	final String delimiter = String.valueOf(strategy.getDelimiter());
	
	// simple test cases
	String line = "A;B;C";
	List<String> token = tokenizer.tokenizeLine(line, strategy, null);
	List<String> expected = new ArrayList<String>() {
	    {
		add("A");
		add("B");
		add("C");
	    }
	};
	assertEquals(expected, token);
	
	// no delimiter test
	line = "A";
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>() {
	    {
		add("A");
	    }
	};
	assertEquals(expected, token);
	
	// empty last token test
	line = "A;";
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>() {
	    {
		add("A");
		add("");
	    }
	};
	assertEquals(expected, token);
	
	// empty token test
	line = delimiter + delimiter;
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>() {
	    {
		add("");
		add("");
		add("");
	    }
	};
	assertEquals(expected, token);
	
	// empty string test
	line = "";
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>();
	assertEquals(expected, token);
	
	// more complex test cases
	// encapsulated delimiter test
	line = "A;B;\"C;D\"";
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>() {
	    {
		add("A");
		add("B");
		add("C;D");
	    }
	};
	assertEquals(expected, token);
	
	// encapsulated delimiter + escaped quote test
	line = "A;B;\"C\"\";\"\"D\"";
	token = tokenizer.tokenizeLine(line, strategy, null);
	expected = new ArrayList<String>() {
	    {
		add("A");
		add("B");
		add("C\";\"D");
	    }
	};
	assertEquals(expected, token);
	
	// encapsulated delimiter + escaped quote + new line test
	line = "A;B;\"C\"\"\n";
	BufferedReader br = new BufferedReader(new StringReader(";\"\"D\""));
	token = tokenizer.tokenizeLine(line, strategy, br);
	expected = new ArrayList<String>() {
	    {
		add("A");
		add("B");
		add("C\"\n;\"D");
	    }
	};
	assertEquals(expected, token);
    }
    
}
