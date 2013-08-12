package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes String values.
 *
 * @author Eike Bergmann
 *
 */
public class StringProcessor implements ValueProcessor<String> {
	/**
	 * There is no need for a conversion. This method simply returns the value
	 *
	 * @return String the value
	 */
	public String processValue(String value) {
		return value;
	}
}
