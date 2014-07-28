package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes boolean values.
 * 
 * @author Eike Bergmann
 */
public class BooleanProcessor implements ValueProcessor<Boolean> {
    
    /**
     * Converts value into a boolean. Returns true, if value is "true" or "1", false otherwise.
     * 
     * @return Boolean the result
     */
    @Override
    public Boolean processValue(String value) {
	if (value == null) {
	    return Boolean.FALSE;
	}
	
	return Boolean.valueOf(value.equals("1") || value.toLowerCase().equals("true"));
    }
}
