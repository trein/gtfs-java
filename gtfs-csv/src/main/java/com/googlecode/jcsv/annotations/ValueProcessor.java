package com.googlecode.jcsv.annotations;

/**
 * The ValueProcessor is used to convert a string value
 * into an object of type E. This is used for annotation parsing.
 *
 * The implementations for the primitives and String are located
 * in com.googlecode.jcsv.annotations.processors
 *
 * @param <E> the destination type
 */
public interface ValueProcessor<E> {
	/**
	 * Converts value into an object of type E.
	 *
	 * @param value the value that should be converted
	 * @return the converted object
	 */
	public E processValue(String value);
}
