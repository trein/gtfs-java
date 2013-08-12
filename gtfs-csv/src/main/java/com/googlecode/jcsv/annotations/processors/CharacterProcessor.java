package com.googlecode.jcsv.annotations.processors;

import com.googlecode.jcsv.annotations.ValueProcessor;

/**
 * Processes character values.
 * 
 * @author Eike Bergmann
 */
public class CharacterProcessor implements ValueProcessor<Character> {
    
    /**
     * Converts value into a character using {@link String#charAt(int)}
     * 
     * @return Character the result
     * @throws IllegalArgumentException if the value's length is not 1
     */
    @Override
    public Character processValue(String value) {
	if ((value == null) || (value.length() != 1)) {
	    throw new IllegalArgumentException(String.format("%s is not a valud character, it's length must be 1", value));
	}
	
	return Character.valueOf(value.charAt(0));
    }
}
