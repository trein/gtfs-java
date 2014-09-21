package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.LineString;

/**
 * An edge that costs nothing to traverse. Used for connecting intersection vertices to the main
 * edge-based graph.
 *
 * @author novalis
 */
public class FreeEdge extends Edge {
    
    private static final long serialVersionUID = 3925814840369402222L;
    
    public FreeEdge(Vertex from, Vertex to) {
        super(from, to);
    }
    
    @Override
    public State traverse(State s0) {
        StateEditor s1 = s0.edit(this);
        s1.incrementWeight(1);
        // do not change mode, which means it may be null at the start of a trip
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
        return "FreeEdge(" + this.fromv + " -> " + this.tov + ")";
    }
}
