package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes byte values.
 * 
 * @author Eike Bergmann
 */
public class ByteProcessor implements ValueProcessor<Byte> {
    
    /**
     * Converts value into a short using {@link Short#parseShort(String)}
     * 
     * @return Byte the result
     */
    @Override
    public Byte processValue(String value) {
	return Byte.valueOf(value);
    }
}
