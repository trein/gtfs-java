package com.googlecode.jcsv.writer.internal;

import java.util.regex.Pattern;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.util.CSVUtil;
import com.googlecode.jcsv.writer.CSVColumnJoiner;

/**
 * This is the default implementation of the CSVColumnJoiner. This implementation follows the csv
 * formatting standard, described in: http://en.wikipedia.org/wiki/Comma-separated_values If you
 * have a more specific csv format, such as constant column widths or columns that do not need to be
 * quoted, you may consider to write a more simple but performant CSVColumnJoiner.
 */
public class CSVColumnJoinerImpl implements CSVColumnJoiner {
    
    @Override
    public String joinColumns(String[] data, CSVStrategy strategy) {
	final String delimiter = String.valueOf(strategy.getDelimiter());
	final String quote = String.valueOf(strategy.getQuoteCharacter());
	final String doubleQuote = quote + quote;
	
	// check each column for delimiter or quote characters
	// and escape them if neccessary
	for (int i = 0; i < data.length; i++) {
	    if (data[i].contains(delimiter) || data[i].contains(quote)) {
		if (data[i].contains(quote)) {
		    data[i] = data[i].replaceAll(Pattern.quote(quote), doubleQuote);
		}
		
		data[i] = quote + data[i] + quote;
	    }
	}
	
	return CSVUtil.implode(data, delimiter);
    }
    
}
