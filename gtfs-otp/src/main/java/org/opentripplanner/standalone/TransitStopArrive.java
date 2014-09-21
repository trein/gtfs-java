package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

public class TransitStopArrive extends OffboardVertex {
    
    private static final long serialVersionUID = 9213431651426739857L;
    private final TransitStop stopVertex;
    
    public TransitStopArrive(Graph g, Stop stop, TransitStop stopVertex) {
        super(g, GtfsLibrary.convertIdToString(stop.getId()) + "_arrive", stop);
        this.stopVertex = stopVertex;
    }
    
    public TransitStop getStopVertex() {
        return this.stopVertex;
    }
    
    @Override
    public AgencyAndId getStopId() {
        return this.stopVertex.getStopId();
    }
}
