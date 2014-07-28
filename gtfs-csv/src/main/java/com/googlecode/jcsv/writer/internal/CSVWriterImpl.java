package com.googlecode.jcsv.writer.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVColumnJoiner;
import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;

public class CSVWriterImpl<E> implements CSVWriter<E> {
    
    private final Writer writer;
    private final CSVStrategy strategy;
    private final CSVEntryConverter<E> entryConverter;
    private final CSVColumnJoiner columnJoiner;
    
    CSVWriterImpl(CSVWriterBuilder<E> builder) {
	this.writer = builder.writer;
	this.strategy = builder.strategy;
	this.entryConverter = builder.entryConverter;
	this.columnJoiner = builder.columnJoiner;
    }
    
    @Override
    public void writeAll(List<E> data) throws IOException {
	for (E e : data) {
	    write(e);
	}
    }
    
    @Override
    public void write(E e) throws IOException {
	StringBuilder sb = new StringBuilder();
	
	String[] columns = this.entryConverter.convertEntry(e);
	String line = this.columnJoiner.joinColumns(columns, this.strategy);
	
	sb.append(line);
	sb.append(System.getProperty("line.separator"));
	
	this.writer.append(sb.toString());
    }
    
    @Override
    public void flush() throws IOException {
	this.writer.flush();
    }
    
    @Override
    public void close() throws IOException {
	this.writer.close();
    }
}
