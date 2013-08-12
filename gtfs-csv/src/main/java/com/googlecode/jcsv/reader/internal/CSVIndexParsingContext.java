package com.googlecode.jcsv.reader.internal;

import java.util.List;

import com.googlecode.jcsv.reader.CSVParsingContext;

public class CSVIndexParsingContext implements CSVParsingContext {
    
    private final List<String> data;
    
    public CSVIndexParsingContext(List<String> data) {
	this.data = data;
    }
    
    @Override
    public String getDataFor(Object key) {
	if (!hasDataFor(key)) {
	    throw new IllegalArgumentException("invalid index");
	}
	int index = Integer.parseInt(key.toString());
	return this.data.get(index);
    }
    
    @Override
    public boolean hasDataFor(Object key) {
	int index = Integer.parseInt(key.toString());
	return (index < this.data.size()) && !this.data.get(index).isEmpty();
    }
    
    @Override
    public String[] getRawData() {
	return this.data.toArray(new String[this.data.size()]);
    }
    
}
