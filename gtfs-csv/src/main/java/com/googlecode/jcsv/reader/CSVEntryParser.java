package com.googlecode.jcsv.reader;

/**
 * The CSVEntryParser receives a line of the csv file and converts it to a java object. The default
 * implementation of this interface is
 * {@link com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser} That implementation just
 * returns the String[] array without any conversion.
 * 
 * @param <E> The Type that the entry parser creates
 */
public interface CSVEntryParser<E> {
    /**
     * Converts a row of the csv file to a java object
     * 
     * @param data a row in the csv file
     * @return the object
     */
    public E parseEntry(CSVParsingContext data);
}
