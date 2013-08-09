package br.com.trein.gtfs.entities;

/**
 * The wheelchair_boarding field identifies whether wheelchair boardings are possible from the specified stop or
 * station. The field can have the following values:<br>
 * 
 * <pre>
 * 0 (or empty) - indicates that there is no accessibility information for the stop
 * 1 - indicates that at least some vehicles at this stop can be boarded by a rider in a wheelchair
 * 2 - wheelchair boarding is not possible at this stop
 * </pre>
 * 
 * <br>
 * When a stop is part of a larger station complex, as indicated by a stop with a parent_station value, the stop's
 * wheelchair_boarding field has the following additional semantics:<br>
 * 
 * <pre>
 * 0 (or empty) - the stop will inherit its wheelchair_boarding value from the parent station, if specified in the parent
 * 1 - there exists some accessible path from outside the station to the specific stop / platform
 * 2 - there exists no accessible path from outside the station to the specific stop / platform
 * </pre>
 * 
 * @author trein
 */
public enum WhellchairType {

}
