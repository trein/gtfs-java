package com.googlecode.jcsv.reader;

import java.io.Closeable;
import java.util.ListIterator;

/**
 * The CacheCSVReader improves the CSVReader with a cache for the read entries. If you need to
 * access the records very often or want to iterate through the list of records back and forth, you
 * might use a cached csv reader. This Interface bundles the methods of the ListIterator and
 * Closeable.
 * 
 * @param <E> the type of the records
 */
public interface CachedCSVReader<E> extends ListIterator<E>, Closeable {
    /**
     * Returns the i's entry of the csv file. If the entry is already in the cache, the entry will
     * be returned directly. If not, the reader reads until the position i is reached or the end of
     * the csv file is reached.
     * 
     * @param i the position
     * @return the entry at position i
     * @throws ArrayIndexOutOfBoundsException if there is no entry at position i
     */
    public E get(int i);
}
