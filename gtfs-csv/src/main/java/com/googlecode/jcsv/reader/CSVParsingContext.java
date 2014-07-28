package com.googlecode.jcsv.reader;

public interface CSVParsingContext {
    
    String getDataFor(Object key);
    
    boolean hasDataFor(Object key);
    
    String[] getRawData();
    
}
