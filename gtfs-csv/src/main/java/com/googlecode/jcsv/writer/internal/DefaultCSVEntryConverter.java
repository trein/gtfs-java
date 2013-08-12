package com.googlecode.jcsv.writer.internal;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * The (simple) default implementation of the CSVEntryConverter. It just returns the input that it
 * reveives. Might be useful if you want wo parse a csv file into a List<String>.
 */
public class DefaultCSVEntryConverter implements CSVEntryConverter<String[]> {
    /**
     * Simply returns the data that it receives.
     * 
     * @param data the incoming data
     * @return the incoming data ;)
     */
    @Override
    public String[] convertEntry(String[] data) {
	return data;
    }
}
