package com.googlecode.jcsv.reader.internal;

import java.util.List;

import com.googlecode.jcsv.reader.CSVParsingContext;

public class CSVHeaderParsingContext implements CSVParsingContext {
    
    private final List<String> data;
    private final CSVHeaderData headerData;
    
    public CSVHeaderParsingContext(CSVHeaderData headerData, List<String> data) {
	this.headerData = headerData;
	this.data = data;
    }
    
    @Override
    public String getDataFor(Object key) {
	validateState();
	int index = this.headerData.getIndexFor(key.toString());
	
	return getDataForIndex(index);
    }
    
    @Override
    public boolean hasDataFor(Object key) {
	validateState();
	
	if (this.headerData.hasValidDataFor(key.toString())) {
	    int index = this.headerData.getIndexFor(key.toString());
	    
	    return hasDataForIndex(index);
	}
	return false;
    }
    
    private void validateState() {
	if (this.headerData == null) {
	    throw new IllegalStateException("header metadata not available");
	}
    }
    
    private String getDataForIndex(int index) {
	if (!hasDataForIndex(index)) {
	    throw new IllegalArgumentException("invalid index");
	}
	return this.data.get(index);
    }
    
    private boolean hasDataForIndex(int index) {
	return (index < this.data.size()) && !this.data.get(index).isEmpty();
    }
    
    @Override
    public String[] getRawData() {
	return this.data.toArray(new String[this.data.size()]);
    }
    
}
