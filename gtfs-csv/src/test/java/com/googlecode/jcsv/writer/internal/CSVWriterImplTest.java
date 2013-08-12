package com.googlecode.jcsv.writer.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;

public class CSVWriterImplTest extends TestCase {

	private static final String NEW_LINE = System.getProperty("line.separator");

	private CSVWriter<Person> csvWriter;
	private final StringWriter stringWriter = new StringWriter();

	@Override
	@Before
	public void setUp() throws Exception {
		// build a new csvWriter that can write person objects
		csvWriter = new CSVWriterBuilder<Person>(stringWriter).entryConverter(new PersonEntryConverter()).build();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		csvWriter.close();
	}

	@Test
	public void testWriteAll() throws IOException {
		List<Person> persons = new ArrayList<Person>();
		persons.add(new Person("Hans", "im \"Glück\"", 16));
		persons.add(new Person("Klaus", "Meyer", 33));

		csvWriter.writeAll(persons);

		String result = stringWriter.toString();
		String expected = "Hans;\"im \"\"Glück\"\"\";16" + NEW_LINE
				+ "Klaus;Meyer;33" + NEW_LINE;
		assertEquals(expected, result);

	}

	@Test
	public void testWrite() throws IOException {
		// write first person
		csvWriter.write(new Person("Hans", "im Glück", 16));
		String result = stringWriter.toString();
		String expected = "Hans;im Glück;16" + NEW_LINE;
		assertEquals(expected, result);

		// write next person
		csvWriter.write(new Person("Klaus", "Meyer", 33));
		result = stringWriter.toString();
		expected = "Hans;im Glück;16" + NEW_LINE
				+ "Klaus;Meyer;33" + NEW_LINE;
		assertEquals(expected, result);
	}

	public void testClose() throws IOException {
		csvWriter.close();
	}

	private static class PersonEntryConverter implements CSVEntryConverter<Person> {
		public String[] convertEntry(Person person) {
			String[] token = new String[3];
			token[0] = person.getFirstName();
			token[1] = person.getLastName();
			token[2] = person.getAge() + "";

			return token;
		}
	}

	private static class Person {
		private final String firstName;
		private final String lastName;
		private final int age;

		public Person(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public int getAge() {
			return age;
		}
	}

}
