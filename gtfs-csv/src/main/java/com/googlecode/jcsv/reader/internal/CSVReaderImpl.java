package com.googlecode.jcsv.reader.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryFilter;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.CSVTokenizer;

public class CSVReaderImpl<E> implements CSVReader<E> {
    
    private final BufferedReader reader;
    private final CSVStrategy strategy;
    private final CSVEntryParser<E> entryParser;
    private final CSVEntryFilter<E> entryFilter;
    private final CSVTokenizer tokenizer;
    
    private boolean firstLineRead = false;
    
    CSVReaderImpl(CSVReaderBuilder<E> builder) {
	this.reader = new BufferedReader(builder.reader);
	this.strategy = builder.strategy;
	this.entryParser = builder.entryParser;
	this.entryFilter = builder.entryFilter;
	this.tokenizer = builder.tokenizer;
    }
    
    @Override
    public List<E> readAll() throws IOException {
	List<E> entries = new ArrayList<E>();
	E entry = null;
	
	while ((entry = readNext()) != null) {
	    entries.add(entry);
	}
	
	return entries;
    }
    
    @Override
    public E readNext() throws IOException {
	if (this.strategy.isSkipHeader() && !this.firstLineRead) {
	    this.reader.readLine();
	}
	
	E entry = null;
	boolean validEntry = false;
	
	do {
	    String line = readLine();
	    
	    if (line == null) {
		return null;
	    }
	    if (isEmptyLine(line)) {
		continue;
	    }
	    if (isCommentLine(line)) {
		continue;
	    }
	    
	    entry = parseEntry(line);
	    validEntry = isValidEntry(entry);
	} while (!validEntry);
	
	this.firstLineRead = true;
	
	return entry;
    }
    
    private E parseEntry(String line) throws IOException {
	List<String> data = this.tokenizer.tokenizeLine(line, this.strategy, this.reader);
	CSVIndexParsingContext context = new CSVIndexParsingContext(data);
	
	return this.entryParser.parseEntry(context);
    }
    
    private boolean isValidEntry(E entry) {
	return this.entryFilter != null ? this.entryFilter.match(entry) : true;
    }
    
    private boolean isEmptyLine(String line) {
	return (line.trim().length() == 0) && this.strategy.isIgnoreEmptyLines();
    }
    
    @Override
    public List<String> readHeader() throws IOException {
	if (this.firstLineRead) {
	    throw new IllegalStateException("can not read header, readHeader() must be the first call on this reader");
	}
	
	String line = readLine();
	if (line == null) {
	    throw new IllegalStateException("reached EOF while reading the header");
	}
	
	List<String> header = this.tokenizer.tokenizeLine(line, this.strategy, this.reader);
	return header;
    }
    
    /**
     * Returns the Iterator for this CSVReaderImpl.
     * 
     * @return Iterator<E> the iterator
     */
    @Override
    public Iterator<E> iterator() {
	return new CSVIterator<E>(this);
    }
    
    /**
     * {@link java.io.Closeable#close()}
     */
    @Override
    public void close() throws IOException {
	this.reader.close();
    }
    
    private boolean isCommentLine(String line) {
	return line.startsWith(String.valueOf(this.strategy.getCommentIndicator()));
    }
    
    /**
     * Reads a line from the given reader and sets the firstLineRead flag.
     * 
     * @return the read line
     * @throws IOException
     */
    private String readLine() throws IOException {
	String line = this.reader.readLine();
	this.firstLineRead = true;
	return line;
    }
    
}
