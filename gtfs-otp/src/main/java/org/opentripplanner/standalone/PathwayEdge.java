package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * A walking pathway as described in GTFS
 */
public class PathwayEdge extends Edge {
    
    private final int traversalTime;
    
    private int wheelchairTraversalTime = -1;
    
    public PathwayEdge(Vertex fromv, Vertex tov, int traversalTime, int wheelchairTraversalTime) {
        super(fromv, tov);
        this.traversalTime = traversalTime;
        this.wheelchairTraversalTime = wheelchairTraversalTime;
    }
    
    public PathwayEdge(Vertex fromv, Vertex tov, int traversalTime) {
        super(fromv, tov);
        this.traversalTime = traversalTime;
    }
    
    private static final long serialVersionUID = -3311099256178798981L;
    
    @Override
    public String getDirection() {
        return null;
    }
    
    @Override
    public double getDistance() {
        return 0;
    }

    public TraverseMode getMode() {
        return TraverseMode.WALK;
    }
    
    @Override
    public LineString getGeometry() {
        Coordinate[] coordinates = new Coordinate[] { getFromVertex().getCoordinate(), getToVertex().getCoordinate() };
        return GeometryUtils.getGeometryFactory().createLineString(coordinates);
    }
    
    @Override
    public String getName() {
        return "pathway";
    }
    
    @Override
    public State traverse(State s0) {
        int time = this.traversalTime;
        if (s0.getOptions().wheelchairAccessible) {
            if (this.wheelchairTraversalTime < 0) { return null; }
            time = this.wheelchairTraversalTime;
        }
        StateEditor s1 = s0.edit(this);
        s1.incrementTimeInSeconds(time);
        s1.incrementWeight(time);
        s1.setBackMode(getMode());
        return s1.makeState();
    }
}
