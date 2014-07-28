package com.googlecode.jcsv.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The MapToColumn annotation is used to instruct the AnnotationEntryParser.
 * You can specify the column in the csv file and which value processor
 * should be used for converting.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapToColumn {
	/**
	 * The column of the data in the csv file.
	 * This parameter is required.
	 *
	 * @return the column in the csv file
	 */
	int column();

	/**
	 * The type of the data.
	 * If set, the appropriate ValueProcessor for this class
	 * will be used to process the data of the csv column.
	 * If not set, the type of the field will be used to find
	 * the appropriate column processor.
	 * This parameter is optional.
	 *
	 * @return the type of the data
	 */
	Class<?> type() default Default.class;

	/**
	 * The default value for the type parameter.
	 */
	public static class Default {}
}