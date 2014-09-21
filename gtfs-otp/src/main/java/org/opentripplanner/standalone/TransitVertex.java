package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

/** Abstract base class for vertices in the GTFS layer of the graph. */
public abstract class TransitVertex extends Vertex {
    
    private static final long serialVersionUID = 53855622892837370L;
    
    private final Stop stop;
    
    public TransitVertex(Graph graph, String label, Stop stop) {
        super(graph, label, stop.getLon(), stop.getLat(), stop.getName());
        this.stop = stop;
    }
    
    /** Get the stop at which this TransitVertex is located */
    public AgencyAndId getStopId() {
        return this.stop.getId();
    }
    
    /** The passenger-facing stop ID/Code (for systems like TriMet that have this feature). */
    public String getStopCode() {
        return this.stop.getCode();
    }
    
    /** The passenger-facing code/name indentifying the platform/quay */
    public String getPlatformCode() {
        return this.stop.getPlatformCode();
    }
    
    /** Stop information need by API */
    public Stop getStop() {
        return this.stop;
    }
    
}
