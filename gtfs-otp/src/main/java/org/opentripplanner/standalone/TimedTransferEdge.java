package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.LineString;

/**
 * An edge represents what GTFS calls a timed transfer. This could also be referred to as a
 * synchronized transfer: vehicles at one stop will wait for passengers alighting from another stop,
 * so there are no walking, minimum transfer time, or schedule slack considerations. In fact, our
 * schedule slack and minimum transfer time implementation requires these special edges to allow
 * 'instantaneous' synchronized transfers. A TimedTransferEdge should connect a stop_arrive vertex
 * to a stop_depart vertex, bypassing the preboard and prealight edges that handle the transfer
 * table and schedule slack. The cost of boarding a vehicle should is added in TransitBoardAlight
 * edges, so it is still taken into account.
 *
 * @author andrewbyrd
 */
public class TimedTransferEdge extends Edge {
    
    private static final long serialVersionUID = 20110730L; // MMMMDDYY
    
    public TimedTransferEdge(Vertex from, Vertex to) {
        super(from, to);
    }
    
    @Override
    public State traverse(State s0) {
        StateEditor s1 = s0.edit(this);
        s1.incrementWeight(1);
        s1.setBackMode(TraverseMode.WALK);
        return s1.makeState();
    }
    
    @Override
    public double getDistance() {
        return 0;
    }
    
    @Override
    public LineString getGeometry() {
        return null;
    }
    
    @Override
    public String getName() {
        return null;
    }
    
    @Override
    public String toString() {
        return "Timed transfer from " + this.fromv + " to " + this.tov;
    }
}
