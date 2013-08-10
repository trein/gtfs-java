package br.com.trein.gtfs.entities;

/**
 * The location_type field identifies whether this stop ID represents a stop or station. If no
 * location type is specified, or the location_type is blank, stop IDs are treated as stops.
 * Stations may have different properties from stops when they are represented on a map or used in
 * trip planning.
 * 
 * @author trein
 */
public enum StopLocationType {
    STOP(0), STATION(1);
    
    private final int code;
    
    private StopLocationType(int code) {
	this.code = code;
    }
    
    /**
     * The location type field can have the following values:<br>
     * 
     * <pre>
     * <li>0 or blank - Stop. A location where passengers board or disembark from a transit vehicle.</li>
     * <li>1 - Station. A physical structure or area that contains one or more stop.</li>
     * </pre>
     * 
     * @return code corresponding to the given type of the stop.
     */
    public int getCode() {
	return this.code;
    }
    
    public boolean isStation() {
	return this == STATION;
    }
    
    public boolean isStop() {
	return this == STOP;
    }
    
}
