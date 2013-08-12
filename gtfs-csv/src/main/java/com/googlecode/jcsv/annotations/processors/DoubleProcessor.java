package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes double values.
 * 
 * @author Eike Bergmann
 */
public class DoubleProcessor implements ValueProcessor<Double> {
    
    /**
     * Converts value into a double using {@link Double#parseDouble(String)}
     * 
     * @return Double the result
     */
    @Override
    public Double processValue(String value) {
	return Double.valueOf(value);
    }
}
