package com.googlecode.jcsv.util;

/**
 * The Builder interfaces indicates that the class can build objects of type E.
 * 
 * @param <E> The type of object that should be build.
 */
public interface Builder<E> {
    /**
     * Finally builds the object.
     * 
     * @return the build object
     */
    public E build();
}
