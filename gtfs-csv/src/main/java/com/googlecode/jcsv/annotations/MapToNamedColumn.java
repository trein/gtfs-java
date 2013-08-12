package com.googlecode.jcsv.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.googlecode.jcsv.annotations.MapToColumn.Default;

/**
 * GTFS mapping in CSV file.
 * 
 * @author trein
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapToNamedColumn {
    
    /**
     * The column of the data in the csv file. This parameter is required.
     * 
     * @return the column in the csv file
     */
    String column();
    
    /**
     * The type of the data. If set, the appropriate ValueProcessor for this class will be used to
     * process the data of the csv column. If not set, the type of the field will be used to find
     * the appropriate column processor. This parameter is optional.
     * 
     * @return the type of the data
     */
    Class<?> type() default Default.class;
    
    /**
     * Used to specify a optional field in CSV file.
     * 
     * @return optional column behavior
     */
    boolean optional() default false;
    
}
