package com.googlecode.jcsv.annotations.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import com.googlecode.jcsv.annotations.ValueProcessor;

@SuppressWarnings("boxing")
public class ValueProcessorsTest {
    
    @Test
    public void testBooleanProcessor() {
	ValueProcessor<Boolean> processor = new BooleanProcessor();
	
	assertTrue(processor.processValue("true"));
	assertTrue(processor.processValue("1"));
	assertFalse(processor.processValue("false"));
	assertFalse(processor.processValue("0"));
	assertFalse(processor.processValue(null));
    }
    
    @Test
    public void testByteProcessor() {
	ValueProcessor<Byte> processor = new ByteProcessor();
	
	assertTrue(0 == processor.processValue("0"));
	assertTrue(0 == processor.processValue("-0"));
	assertTrue(15 == processor.processValue("15"));
	assertTrue(-50 == processor.processValue("-50"));
    }
    
    @Test(expected = NumberFormatException.class)
    public void testFailByteProcessor() {
	ValueProcessor<Byte> processor = new ByteProcessor();
	
	processor.processValue("128");
    }
    
    @Test
    public void testCharacterProcessor() {
	ValueProcessor<Character> processor = new CharacterProcessor();
	
	assertTrue('a' == processor.processValue("a"));
	assertTrue(' ' == processor.processValue(" "));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFailCharacterProcessor() {
	ValueProcessor<Character> processor = new CharacterProcessor();
	
	processor.processValue(null);
    }
    
    @Test
    public void testDoubleProcessor() {
	ValueProcessor<Double> processor = new DoubleProcessor();
	
	assertTrue(1.234 == processor.processValue("1.234"));
	assertTrue(-0.1 == processor.processValue("-0.1"));
	assertTrue(0 == processor.processValue("0"));
	assertTrue(1000 == processor.processValue("1e3"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFailDoubleProcessor() {
	ValueProcessor<Double> processor = new DoubleProcessor();
	
	processor.processValue("1e3g");
    }
    
    @Test
    public void testFloatProcessor() {
	ValueProcessor<Float> processor = new FloatProcessor();
	
	assertTrue(1.234f == processor.processValue("1.234"));
	assertTrue(-0.1f == processor.processValue("-0.1f"));
	assertTrue(0 == processor.processValue("0f"));
	assertTrue(1000 == processor.processValue("1e3f"));
    }
    
    @Test(expected = NullPointerException.class)
    public void testFailFloatProcessor() {
	ValueProcessor<Float> processor = new FloatProcessor();
	
	processor.processValue(null);
    }
    
    @Test
    public void testIntegerProcessor() {
	ValueProcessor<Integer> processor = new IntegerProcessor();
	
	assertTrue(12 == processor.processValue("12"));
	assertTrue(-12 == processor.processValue("-12"));
	assertTrue(0 == processor.processValue("0"));
    }
    
    @Test(expected = NumberFormatException.class)
    public void testFailIntegerProcessor() {
	ValueProcessor<Integer> processor = new IntegerProcessor();
	
	processor.processValue("pi");
    }
    
    @Test
    public void testLongProcessor() {
	ValueProcessor<Long> processor = new LongProcessor();
	
	assertTrue(12 == processor.processValue("12"));
	assertTrue(-12 == processor.processValue("-12"));
	assertTrue(0 == processor.processValue("0"));
    }
    
    @Test(expected = NumberFormatException.class)
    public void testFailLongProcessor() {
	ValueProcessor<Integer> processor = new IntegerProcessor();
	
	processor.processValue("pi");
    }
    
    @Test
    public void testShortProcessor() {
	ValueProcessor<Short> processor = new ShortProcessor();
	
	assertTrue(12 == processor.processValue("12"));
	assertTrue(-12 == processor.processValue("-12"));
	assertTrue(0 == processor.processValue("0"));
    }
    
    @Test(expected = NumberFormatException.class)
    public void testFailShortProcessor() {
	ValueProcessor<Short> processor = new ShortProcessor();
	
	processor.processValue("32768");
    }
    
    @Test
    public void testStringProcessor() {
	ValueProcessor<String> processor = new StringProcessor();
	
	assertEquals("Test", processor.processValue("Test"));
	assertFalse("test".equals(processor.processValue("Test")));
	assertEquals(null, processor.processValue(null));
    }
    
    @Test
    public void testDateProcessor() throws ParseException {
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
	ValueProcessor<Date> processor = new DateProcessor(df);
	
	assertEquals(df.parse("12.12.2012"), processor.processValue("12.12.2012"));
	assertEquals(df.parse("01.01.1970"), processor.processValue("01.01.1970"));
	assertEquals(10800000, processor.processValue("01.01.1970").getTime());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFailDateProcessor() throws ParseException {
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
	ValueProcessor<Date> processor = new DateProcessor(df);
	
	processor.processValue("12/12/2012");
    }
}
