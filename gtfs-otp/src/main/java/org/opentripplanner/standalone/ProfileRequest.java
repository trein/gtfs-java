package org.opentripplanner.standalone;

import org.joda.time.LocalDate;

/**
 * All the modifiable paramters for profile routing.
 */
public class ProfileRequest {
    
    public LatLon from;
    public LatLon to;
    public int fromTime;
    public int toTime;
    public float walkSpeed;
    public float bikeSpeed;
    public int streetTime;
    public int accessTime;
    public LocalDate date;
    public Option.SortOrder orderBy;
    public int limit;
    public TraverseModeSet modes;
    public boolean analyst = false; // if true, propagate travel times out to street network
    
}
