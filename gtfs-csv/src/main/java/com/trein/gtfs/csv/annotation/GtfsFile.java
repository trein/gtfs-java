package com.trein.gtfs.csv.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GTFS CSV file.
 *
 * @author trein
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GtfsFile {

    /**
     * File name describing the GTFS entity
     *
     * @return the column in the csv file
     */
    String value();

    /**
     * Used to specify a optional CSV file.
     *
     * @return optional column behavior
     */
    boolean optional() default false;
}
