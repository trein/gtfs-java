package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

public class TransitStop extends TransitStationStop {
    private final TraverseModeSet modes = new TraverseModeSet();
    
    private static final long serialVersionUID = 1L;
    
    private final boolean wheelchairEntrance;
    
    private final boolean isEntrance;
    
    /**
     * For stops that are deep underground, there is a time cost to entering and exiting the stop;
     * all stops are assumed to be at street level unless we have configuration to the contrary
     */
    private int streetToStopTime = 0;
    
    /*
     * We sometimes want a reference to a TransitStop's corresponding arrive or depart vertex.
     * Rather than making a Map in the GraphIndex, we just store them here. This should also help
     * make the GTFS-loading context object unnecessary, and eventually help eliminate explicit
     * transit edges.
     */
    public TransitStopArrive arriveVertex;
    public TransitStopDepart departVertex;
    
    public TransitStop(Graph graph, Stop stop) {
        super(graph, stop);
        this.wheelchairEntrance = stop.getWheelchairBoarding() == 1;
        this.isEntrance = stop.getLocationType() == 2;
    }
    
    public boolean hasWheelchairEntrance() {
        return this.wheelchairEntrance;
    }
    
    public boolean isEntrance() {
        return this.isEntrance;
    }
    
    public boolean hasEntrances() {
        for (Edge e : this.getOutgoing()) {
            if (e instanceof PathwayEdge) { return true; }
        }
        return false;
    }
    
    public int getStreetToStopTime() {
        return this.streetToStopTime;
    }
    
    public void setStreetToStopTime(int streetToStopTime) {
        this.streetToStopTime = streetToStopTime;
    }
    
    public TraverseModeSet getModes() {
        return this.modes;
    }
    
    public void addMode(TraverseMode mode) {
        this.modes.setMode(mode, true);
    }

    public boolean isStreetLinkable() {
        return isEntrance() || !hasEntrances();
    }
}
