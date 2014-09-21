package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

import com.vividsolutions.jts.geom.LineString;

/**
 * FrequencyHops and PatternHops have start/stop Stops
 * 
 * @author novalis
 */
public interface HopEdge {
    
    Stop getEndStop();
    
    Stop getBeginStop();
    
    void setGeometry(LineString geometry);
    
}
