package com.googlecode.jcsv.writer.internal;

import java.io.Writer;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.util.Builder;
import com.googlecode.jcsv.writer.CSVColumnJoiner;
import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;

/**
 * The builder that creates the CSVWriterImpl instance.
 * 
 * @param <E> The Type of the records
 */
public class CSVWriterBuilder<E> implements Builder<CSVWriter<E>> {
    final Writer writer;
    CSVStrategy strategy = CSVStrategy.DEFAULT;
    CSVEntryConverter<E> entryConverter;
    CSVColumnJoiner columnJoiner = new CSVColumnJoinerImpl();
    
    /**
     * Creates a Builder for the CSVWriterImpl
     * 
     * @param writer the character output stream
     */
    public CSVWriterBuilder(Writer writer) {
	this.writer = writer;
    }
    
    /**
     * Sets the strategy that the CSVWriterImpl will use.
     * 
     * @param strategy the csv strategy
     * @return this builder
     */
    public CSVWriterBuilder<E> strategy(CSVStrategy strategy) {
	this.strategy = strategy;
	return this;
    }
    
    /**
     * Sets the entry converter that the CSVWriterImpl will use.
     * 
     * @param entryConverter the entry converter
     * @return this builder
     */
    public CSVWriterBuilder<E> entryConverter(CSVEntryConverter<E> entryConverter) {
	this.entryConverter = entryConverter;
	return this;
    }
    
    /**
     * Sets the column joiner strategy that the CSVWriterImpl will use. If you don't specify your
     * own csv tokenizer strategy, the default column joiner will be used.
     * {@link com.googlecode.jcsv.writer.internal.CSVColumnJoinerImpl}
     * 
     * @param columnJoiner the column joiner
     * @return this builder
     */
    public CSVWriterBuilder<E> columnJoiner(CSVColumnJoiner columnJoiner) {
	this.columnJoiner = columnJoiner;
	return this;
    }
    
    /**
     * Builds the CSVWriterImpl, using the specified configuration
     * 
     * @return the CSVWriterImpl instance
     */
    @Override
    public CSVWriter<E> build() {
	if (this.entryConverter == null) {
	    throw new IllegalStateException("you have to specify an entry converter");
	}
	
	return new CSVWriterImpl<E>(this);
    }
    
    /**
     * Returns a default configured CSVWriterImpl<String[]>. It uses the DefaultCSVEntryParser that
     * allows you to write a String[] arrayas an entry in your csv file.
     * 
     * @param writer the character output stream
     * @return the CSVWriterImpl
     */
    public static CSVWriter<String[]> newDefaultWriter(Writer writer) {
	return new CSVWriterBuilder<String[]>(writer).entryConverter(new DefaultCSVEntryConverter()).build();
    }
}
