package com.trein.gtfs.csv.reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVHeaderData {
    
    private final Map<String, Integer> indexMapping = new HashMap<String, Integer>();
    
    public CSVHeaderData(List<String> header) {
	for (int index = 0; index < header.size(); index++) {
	    this.indexMapping.put(header.get(index), Integer.valueOf(index));
	}
    }
    
    public int getIndexFor(String header) {
	Integer index = this.indexMapping.get(header);
	return index.intValue();
    }
    
    public boolean hasValidDataFor(String header) {
	return this.indexMapping.get(header) != null;
    }
}
