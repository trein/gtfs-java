package com.googlecode.jcsv.annotations.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcsv.annotations.ValueProcessor;
import com.googlecode.jcsv.annotations.processors.IntegerProcessor;
import com.googlecode.jcsv.annotations.processors.StringProcessor;

public class ValueProcessorProviderTest {
	
	private ValueProcessorProvider provider;
	
	@Before
	public void setUp() {
		provider = new ValueProcessorProvider();
		provider.removeValueProcessor(Integer.class);
	}
	
	@Test
	public void testRegisterAndGetValueProcessor() {
		ValueProcessor<Integer> integerProcessor = new IntegerProcessor();
		provider.registerValueProcessor(Integer.class, integerProcessor);
		
		assertEquals(integerProcessor, provider.getValueProcessor(Integer.class));
		assertEquals(integerProcessor, provider.getValueProcessor(int.class));
		
		// add a processor for a sub class of type E
		ValueProcessor<String> stringProcessor = new StringProcessor();
		provider.registerValueProcessor(CharSequence.class, stringProcessor);
		
		assertEquals(stringProcessor, provider.getValueProcessor(CharSequence.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFailRegisterValueProcessor() {
		ValueProcessor<Integer> integerProcessor = new IntegerProcessor();
		
		provider.registerValueProcessor(Integer.class, integerProcessor);
		provider.registerValueProcessor(Integer.class, integerProcessor);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRemoveValueProcessor() {
		ValueProcessor<Integer> integerProcessor = new IntegerProcessor();
		provider.registerValueProcessor(Integer.class, integerProcessor);
		
		assertEquals(integerProcessor, provider.getValueProcessor(Integer.class));
		
		provider.removeValueProcessor(Integer.class);
		provider.getValueProcessor(Integer.class);
	}
}
