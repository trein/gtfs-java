package com.googlecode.jcsv.reader.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.CachedCSVReader;

public class CachedCSVReaderImpl<E> implements CachedCSVReader<E> {
    
    private final CSVReader<E> reader;
    
    private final List<E> cachedEntries;
    private int currentIndex;
    
    public CachedCSVReaderImpl(CSVReader<E> reader) {
	this.reader = reader;
	this.cachedEntries = new ArrayList<E>();
	
	this.currentIndex = -1;
    }
    
    @Override
    public boolean hasNext() {
	if ((this.currentIndex + 1) >= this.cachedEntries.size()) {
	    cacheNextEntry();
	}
	
	return (this.currentIndex + 1) < this.cachedEntries.size();
    }
    
    @SuppressWarnings("boxing")
    @Override
    public E next() {
	if (!hasNext()) {
	    throw new NoSuchElementException(String.format("size: %s, index: %s", this.cachedEntries.size(),
		    this.currentIndex + 1));
	}
	
	this.currentIndex++;
	return this.cachedEntries.get(this.currentIndex);
    }
    
    @Override
    public boolean hasPrevious() {
	return this.currentIndex > 0;
    }
    
    @SuppressWarnings("boxing")
    @Override
    public E previous() {
	if (!hasPrevious()) {
	    throw new NoSuchElementException(String.format("size: %s, index: %s", this.cachedEntries.size(),
		    this.currentIndex - 1));
	}
	
	this.currentIndex--;
	return this.cachedEntries.get(this.currentIndex);
    }
    
    @Override
    public int nextIndex() {
	if (this.currentIndex >= this.cachedEntries.size()) {
	    cacheNextEntry();
	}
	
	if (this.currentIndex >= this.cachedEntries.size()) {
	    return this.cachedEntries.size();
	}
	
	return this.currentIndex + 1;
    }
    
    @Override
    public int previousIndex() {
	return this.currentIndex - 1;
    }
    
    @SuppressWarnings("boxing")
    @Override
    public E get(int index) {
	if (index < 0) {
	    throw new IllegalArgumentException("i has to be greater 0, but was " + index);
	}
	
	readUntil(index);
	
	if (this.cachedEntries.size() < index) {
	    throw new ArrayIndexOutOfBoundsException(String.format("size: %s, index: %s", this.cachedEntries.size(), index));
	}
	
	return this.cachedEntries.get(index);
    }
    
    @Override
    public void remove() {
	throw new UnsupportedOperationException("remove not allowed");
    }
    
    @Override
    public void set(Object e) {
	throw new UnsupportedOperationException("set not allowed");
    }
    
    @Override
    public void add(Object e) {
	throw new UnsupportedOperationException("add not allowed");
    }
    
    @Override
    public void close() throws IOException {
	this.reader.close();
    }
    
    private void readUntil(int i) {
	while (cacheNextEntry() && (this.cachedEntries.size() <= i)) {
	    ;
	}
    }
    
    private boolean cacheNextEntry() {
	boolean success = false;
	try {
	    E entry = this.reader.readNext();
	    if (entry != null) {
		this.cachedEntries.add(entry);
		success = true;
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	
	return success;
    }
    
}
