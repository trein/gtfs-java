package com.googlecode.jcsv.reader.internal;

import java.lang.reflect.Field;

import com.googlecode.jcsv.annotations.MapToColumn;
import com.googlecode.jcsv.annotations.ValueProcessor;
import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVParsingContext;

/**
 * Parses a csv entry, based on an annotated class.
 * 
 * @author Eike Bergmann
 * @param <E> the type of the csv entries
 */
public class AnnotationEntryParser<E> implements CSVEntryParser<E> {
    
    private final Class<E> clazz;
    
    private final ValueProcessorProvider provider;
    
    /**
     * Constructs a AnnotationEntryParser for type E.
     * 
     * @param clazz the annotated class, and the class of the csv entries
     */
    public AnnotationEntryParser(Class<E> clazz, ValueProcessorProvider provider) {
	this.clazz = clazz;
	this.provider = provider;
    }
    
    /**
     * {@link CSVEntryParser#parseEntry(CSVParsingContext)}
     */
    @Override
    public E parseEntry(CSVParsingContext data) {
	// create the instance
	E entry = newClassIntance();
	
	// fill the object with data
	fillObject(entry, data);
	
	return entry;
    }
    
    /**
     * Creates a new instance of type E.
     * 
     * @throws RuntimeException thrown if the class can not be instantiated or accessed
     * @return the new instance
     */
    private E newClassIntance() {
	E entry;
	try {
	    entry = this.clazz.newInstance();
	} catch (InstantiationException ie) {
	    throw new RuntimeException(String.format("can not instantiate class %s", this.clazz.getName()), ie);
	} catch (IllegalAccessException iae) {
	    throw new RuntimeException(String.format("can not access class %s", this.clazz.getName()), iae);
	}
	
	return entry;
    }
    
    /**
     * Fill the instance of E with the data of the csv row. This method iterates over the
     * annotations that are present in the target class and uses the appropriate value processors to
     * set the data.
     * 
     * @param entry The created instance of E
     * @param data The data that should be set
     */
    private void fillObject(E entry, CSVParsingContext data) {
	for (Field field : entry.getClass().getDeclaredFields()) {
	    // check if there is a MapToColumn Annotation
	    MapToColumn mapAnnotation = field.getAnnotation(MapToColumn.class);
	    if (mapAnnotation != null) {
		// read the annotation column
		int column = mapAnnotation.column();
		
		// read the annotation type. If type is Default.class, then
		// the type of the field will be used.
		Class<?> type;
		if (mapAnnotation.type().equals(MapToColumn.Default.class)) {
		    // use the field type
		    type = field.getType();
		} else {
		    // use the annotated type
		    type = mapAnnotation.type();
		}
		
		// load the appropriate value processor
		ValueProcessor<?> vp = this.provider.getValueProcessor(type);
		
		// use the value processor to convert the string data
		Object value = vp.processValue(data.getDataFor(Integer.valueOf(column)));
		
		// make the field accessible and remember its state
		boolean wasAccessible = field.isAccessible();
		field.setAccessible(true);
		
		// try to set the field's value
		try {
		    field.set(entry, value);
		} catch (IllegalArgumentException iae) {
		    throw new RuntimeException(String.format("can not set value %s for type %s", value, type), iae);
		} catch (IllegalAccessException iae) {
		    throw new RuntimeException(String.format("can not access field %s", field), iae);
		}
		
		// re-set the accessible flag
		field.setAccessible(wasAccessible);
	    }
	}
    }
}
