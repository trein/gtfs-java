package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * A relatively low cost edge for alighting from an elevator. All narrative generation is done by
 * the ElevatorAlightEdge (other edges are silent), because it is the only edge that knows where the
 * user is to get off.
 * 
 * @author mattwigway
 */
public class ElevatorAlightEdge extends Edge implements ElevatorEdge {
    
    private static final long serialVersionUID = 3925814840369402222L;
    
    /**
     * This is the level of this elevator exit, used in narrative generation.
     */
    private final String level;
    
    /**
     * The polyline geometry of this edge. It's generally a polyline with two coincident points, but
     * some elevators have horizontal dimension, e.g. the ones on the Eiffel Tower.
     */
    private final LineString the_geom;

    /**
     * @param level It's a float for future expansion.
     */
    public ElevatorAlightEdge(ElevatorOnboardVertex from, ElevatorOffboardVertex to, String level) {
        super(from, to);
        this.level = level;
        
        // set up the geometry
        Coordinate[] coords = new Coordinate[2];
        coords[0] = new Coordinate(from.getX(), from.getY());
        coords[1] = new Coordinate(to.getX(), to.getY());
        this.the_geom = GeometryUtils.getGeometryFactory().createLineString(coords);
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
        return this.the_geom;
    }
    
    /**
     * The level from OSM is the name
     */
    @Override
    public String getName() {
        return this.level;
    }
    
    /**
     * The name is not bogus; it's level n from OSM.
     * 
     * @author mattwigway
     */
    @Override
    public boolean hasBogusName() {
        return false;
    }

    @Override
    public String toString() {
        return "ElevatorAlightEdge(" + this.fromv + " -> " + this.tov + ")";
    }
}
