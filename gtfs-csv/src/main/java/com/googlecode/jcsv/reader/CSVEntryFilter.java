package com.googlecode.jcsv.reader;

/**
 * The CSVEntryFilter is used to filter the records of a csv file.
 * 
 * @param <E> the type of the records
 */
public interface CSVEntryFilter<E> {
    
    /**
     * Checks whether the object e matches this filter.
     * 
     * @param e The object that is to be tested
     * @return true, if e matches this filter
     */
    public boolean match(E e);
}
