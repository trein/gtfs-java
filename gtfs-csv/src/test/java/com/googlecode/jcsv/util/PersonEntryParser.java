package com.googlecode.jcsv.util;

import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVParsingContext;

public class PersonEntryParser implements CSVEntryParser<Person> {
    
    @SuppressWarnings("boxing")
    @Override
    public Person parseEntry(CSVParsingContext context) {
	String firstName = context.getDataFor(0);
	String lastName = context.getDataFor(1);
	int age = Integer.parseInt(context.getDataFor(2));
	
	return new Person(firstName, lastName, age);
    }
}
