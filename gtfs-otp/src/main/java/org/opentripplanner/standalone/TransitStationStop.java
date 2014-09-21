package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

public abstract class TransitStationStop extends OffboardVertex {
    private static final long serialVersionUID = 1L;
    
    public TransitStationStop(Graph graph, Stop stop) {
        super(graph, GtfsLibrary.convertIdToString(stop.getId()), stop);
    }
}
