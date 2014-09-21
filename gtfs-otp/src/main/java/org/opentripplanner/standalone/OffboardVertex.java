package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

public abstract class OffboardVertex extends TransitVertex {
    
    private static final long serialVersionUID = 1L;
    
    public OffboardVertex(Graph graph, String label, Stop stop) {
        super(graph, label, stop);
    }
    
}
