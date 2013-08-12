package com.googlecode.jcsv.reader.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.util.Person;
import com.googlecode.jcsv.util.PersonEntryParser;

@SuppressWarnings("boxing")
public class CachedCSVReaderImplTest {
    
    private CachedCSVReaderImpl<Person> cachedReader;
    
    @Before
    public void setUp() throws Exception {
	Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/persons.csv"));
	CSVStrategy strategy = new CSVStrategy(';', '"', '#', true, true);
	CSVReader<Person> csvReader = new CSVReaderBuilder<Person>(reader).entryParser(new PersonEntryParser())
	        .strategy(strategy).build();
	this.cachedReader = new CachedCSVReaderImpl<Person>(csvReader);
    }
    
    @After
    public void tearDown() throws Exception {
	this.cachedReader.close();
    }
    
    @Test
    public void testHasNext() {
	// The cachedReader has 2 entries
	assertTrue(this.cachedReader.hasNext());
	this.cachedReader.next();
	assertTrue(this.cachedReader.hasNext());
	this.cachedReader.next();
	assertFalse(this.cachedReader.hasNext());
    }
    
    @Test
    public void testNext() {
	Person result = this.cachedReader.next();
	Person expected = new Person("Hans", "im Glück", 16);
	assertEquals(expected, result);
	
	result = this.cachedReader.next();
	expected = new Person("Klaus", "Meyer", 33);
	assertEquals(expected, result);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testListBounds() {
	this.cachedReader.next();
	this.cachedReader.next();
	this.cachedReader.next();
    }
    
    @Test
    public void testHasPrevious() {
	assertFalse(this.cachedReader.hasPrevious());
	
	this.cachedReader.next();
	assertFalse(this.cachedReader.hasPrevious());
	
	this.cachedReader.next();
	assertTrue(this.cachedReader.hasPrevious());
	
	this.cachedReader.previous();
	assertFalse(this.cachedReader.hasPrevious());
    }
    
    @Test
    public void testPrevious() {
	Person expected = this.cachedReader.next();
	this.cachedReader.next();
	
	assertSame(expected, this.cachedReader.previous());
	
    }
    
    @Test
    public void testNextIndex() {
	// first call has to return 0
	assertSame(0, this.cachedReader.nextIndex());
	
	// move to the first entry
	this.cachedReader.next();
	assertSame(1, this.cachedReader.nextIndex());
	
	// move to the end of the list, there will be no next entry
	this.cachedReader.next();
	assertSame(2, this.cachedReader.nextIndex());
    }
    
    @Test
    public void testPreviousIndex() {
	this.cachedReader.next();
	
	// first call has to return -1, hence we are at the beginning of the list
	assertSame(-1, this.cachedReader.previousIndex());
	
	// move to the next entry
	this.cachedReader.next();
	assertSame(0, this.cachedReader.previousIndex());
    }
    
    @Test
    public void testGet() {
	Person result = this.cachedReader.get(1);
	Person expected = new Person("Klaus", "Meyer", 33);
	assertEquals(expected, result);
	
	result = this.cachedReader.get(0);
	expected = new Person("Hans", "im Glück", 16);
	assertEquals(expected, result);
	
	result = this.cachedReader.get(1);
	expected = new Person("Klaus", "Meyer", 33);
	assertEquals(expected, result);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetFails() {
	this.cachedReader.get(42);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
	this.cachedReader.remove();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSet() {
	this.cachedReader.set(null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd() {
	this.cachedReader.add(null);
    }
}
