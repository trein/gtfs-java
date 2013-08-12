package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes float values.
 * 
 * @author Eike Bergmann
 */
public class FloatProcessor implements ValueProcessor<Float> {
    
    /**
     * Converts value into a float using {@link Float#parseFloat(String)}
     * 
     * @return Float the result
     */
    @Override
    public Float processValue(String value) {
	return Float.valueOf(value);
    }
}
