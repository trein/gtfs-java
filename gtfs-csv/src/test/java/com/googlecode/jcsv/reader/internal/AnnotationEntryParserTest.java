package com.googlecode.jcsv.reader.internal;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.googlecode.jcsv.annotations.MapToColumn;
import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVEntryParser;

public class AnnotationEntryParserTest {
    
    @Test
    public void testParseEntry() throws ParseException {
	ValueProcessorProvider provider = new ValueProcessorProvider();
	CSVEntryParser<Person> entryParser = new AnnotationEntryParser<Person>(Person.class, provider);
	
	DateFormat df = DateFormat.getDateInstance();
	Person expected = new Person("Hans", "im Glück", 18, df.parse("12.12.2012"));
	
	String[] data = { "Hans", "im Glück", "18", "12.12.2012" };
	List<String> contextData = Arrays.asList(data);
	
	Person result = entryParser.parseEntry(new CSVIndexParsingContext(contextData));
	assertEquals(expected, result);
    }
    
    private static class Person {
	@MapToColumn(column = 0)
	private String firstName;
	
	@MapToColumn(column = 1)
	private String lastName;
	
	@MapToColumn(column = 2)
	private int age;
	
	@MapToColumn(column = 3)
	private Date birthday;
	
	@SuppressWarnings("unused")
	public Person() {
	}
	
	public Person(String firstName, String lastName, int age, Date birthday) {
	    this.firstName = firstName;
	    this.lastName = lastName;
	    this.age = age;
	    this.birthday = birthday;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = (prime * result) + this.age;
	    result = (prime * result) + ((this.birthday == null) ? 0 : this.birthday.hashCode());
	    result = (prime * result) + ((this.firstName == null) ? 0 : this.firstName.hashCode());
	    result = (prime * result) + ((this.lastName == null) ? 0 : this.lastName.hashCode());
	    return result;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    Person other = (Person) obj;
	    if (this.age != other.age) {
		return false;
	    }
	    if (this.birthday == null) {
		if (other.birthday != null) {
		    return false;
		}
	    } else if (!this.birthday.equals(other.birthday)) {
		return false;
	    }
	    if (this.firstName == null) {
		if (other.firstName != null) {
		    return false;
		}
	    } else if (!this.firstName.equals(other.firstName)) {
		return false;
	    }
	    if (this.lastName == null) {
		if (other.lastName != null) {
		    return false;
		}
	    } else if (!this.lastName.equals(other.lastName)) {
		return false;
	    }
	    return true;
	}
    }
}
