package com.googlecode.jcsv.writer;

/**
 * The CSVEntryConverter receives a java object and converts it into a String[] array that will be
 * written to the output stream.
 * 
 * @param <E> The Type that will be converted
 */
public interface CSVEntryConverter<E> {
    /**
     * Converts an object of type E into a String[] array, that will be written into the csv file.
     * 
     * @param e that object that will be converted
     * @return the data
     */
    public String[] convertEntry(E e);
}
