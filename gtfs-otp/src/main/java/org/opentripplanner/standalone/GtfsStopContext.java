package org.opentripplanner.standalone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;

/** Retains graph-wide information between GTFSPatternHopFactory runs on different feeds. */
public class GtfsStopContext {
    
    public HashSet<AgencyAndId> stops = new HashSet<AgencyAndId>();
    
    public Map<Stop, TransitStationStop> stationStopNodes = new HashMap<Stop, TransitStationStop>();
    
    // FIXME these are stored in the stop vertices now, can remove
    public Map<Stop, TransitStopArrive> stopArriveNodes = new HashMap<Stop, TransitStopArrive>();
    
    // FIXME these are stored in the stop vertices now, can remove
    public Map<Stop, TransitStopDepart> stopDepartNodes = new HashMap<Stop, TransitStopDepart>();
    
    public Map<T2<Stop, Trip>, Vertex> patternArriveNodes = new HashMap<T2<Stop, Trip>, Vertex>();
    
    // exemplar
    public Map<T2<Stop, Trip>, Vertex> patternDepartNodes = new HashMap<T2<Stop, Trip>, Vertex>();
    
    // Why?
    public HashMap<TripPattern, Integer> tripPatternIds = new HashMap<TripPattern, Integer>();
    
}
