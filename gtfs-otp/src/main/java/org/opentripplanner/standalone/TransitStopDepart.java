package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;

/* Note that this is not a subclass of TransitStop, to avoid it being linked to the street network */
public class TransitStopDepart extends OffboardVertex {
    
    private static final long serialVersionUID = 5353034364687763358L;
    private final TransitStop stopVertex;
    
    public TransitStopDepart(Graph graph, Stop stop, TransitStop stopVertex) {
        super(graph, GtfsLibrary.convertIdToString(stop.getId()) + "_depart", stop);
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
