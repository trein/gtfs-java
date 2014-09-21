package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

public class PatternStopVertex extends OnboardVertex {
    
    private static final long serialVersionUID = 1L;

    public PatternStopVertex(Graph g, String label, TripPattern tripPattern, Stop stop) {
        super(g, label, tripPattern, stop);
    }
    
}
