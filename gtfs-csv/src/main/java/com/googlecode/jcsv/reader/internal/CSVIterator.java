package com.googlecode.jcsv.reader.internal;

import java.io.IOException;
import java.util.Iterator;

import com.googlecode.jcsv.reader.CSVReader;

public class CSVIterator<E> implements Iterator<E> {
    
    private E nextEntry;
    private final CSVReader<E> reader;
    
    public CSVIterator(CSVReader<E> reader) {
	this.reader = reader;
    }
    
    @Override
    public boolean hasNext() {
	if (this.nextEntry != null) {
	    return true;
	}
	
	try {
	    this.nextEntry = this.reader.readNext();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	
	return this.nextEntry != null;
    }
    
    @Override
    public E next() {
	E entry = null;
	if (this.nextEntry != null) {
	    entry = this.nextEntry;
	    this.nextEntry = null;
	} else {
	    try {
		entry = this.reader.readNext();
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
	
	return entry;
    }
    
    @Override
    public void remove() {
	throw new UnsupportedOperationException("this iterator doesn't support object deletion");
    }
}
