package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

public abstract class OnboardVertex extends TransitVertex {
    
    private static final long serialVersionUID = 1L;
    
    private final TripPattern tripPattern; // set to null for non-pattern vertices
    
    // (or just use patterns for everything, eliminating simple hops)

    public OnboardVertex(Graph g, String label, TripPattern tripPattern, Stop stop) {
        super(g, label, stop);
        this.tripPattern = tripPattern;
    }
    
    public TripPattern getTripPattern() {
        return this.tripPattern;
    }

}
