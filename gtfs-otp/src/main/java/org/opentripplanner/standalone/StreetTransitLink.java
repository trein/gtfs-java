package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Trip;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * This represents the connection between a street vertex and a transit vertex where going from the
 * street to the vehicle is immediate -- such as at a curbside bus stop.
 */
public class StreetTransitLink extends Edge {
    
    private static final long serialVersionUID = -3311099256178798981L;
    static final int STL_TRAVERSE_COST = 1;
    
    private final boolean wheelchairAccessible;
    
    private final TransitStop transitStop;
    
    public StreetTransitLink(StreetVertex fromv, TransitStop tov, boolean wheelchairAccessible) {
        super(fromv, tov);
        this.transitStop = tov;
        this.wheelchairAccessible = wheelchairAccessible;
    }
    
    public StreetTransitLink(TransitStop fromv, StreetVertex tov, boolean wheelchairAccessible) {
        super(fromv, tov);
        this.transitStop = fromv;
        this.wheelchairAccessible = wheelchairAccessible;
    }
    
    @Override
    public String getDirection() {
        return null;
    }
    
    @Override
    public double getDistance() {
        return 0;
    }
    
    @Override
    public LineString getGeometry() {
        Coordinate[] coordinates = new Coordinate[] { this.fromv.getCoordinate(), this.tov.getCoordinate() };
        return GeometryUtils.getGeometryFactory().createLineString(coordinates);
    }
    
    public TraverseMode getMode() {
        return TraverseMode.LEG_SWITCH;
    }
    
    @Override
    public String getName() {
        return "street transit link";
    }
    
    @Override
    public State traverse(State s0) {
        RoutingRequest req = s0.getOptions();
        if (s0.getOptions().wheelchairAccessible && !this.wheelchairAccessible) { return null; }
        // Do not check here whether any transit modes are selected. A check for the presence of
        // transit modes will instead be done in the following PreBoard edge.
        // This allows searching for nearby transit stops using walk-only options.
        StateEditor s1 = s0.edit(this);
        
        /* Only enter stations in CAR mode if parking is not required (kiss and ride) */
        /*
         * Note that in arriveBy searches this is double-traversing link edges to fork the state
         * into both WALK and CAR mode. This is an insane hack.
         */
        if (s0.getNonTransitMode() == TraverseMode.CAR) {
            if (req.kissAndRide && !s0.isCarParked()) {
                s1.setCarParked(true);
            } else {
                return null;
            }
        }
        s1.incrementTimeInSeconds(this.transitStop.getStreetToStopTime() + STL_TRAVERSE_COST);
        s1.incrementWeight(STL_TRAVERSE_COST + this.transitStop.getStreetToStopTime());
        s1.setBackMode(TraverseMode.LEG_SWITCH);
        return s1.makeState();
    }
    
    @Override
    public State optimisticTraverse(State s0) {
        StateEditor s1 = s0.edit(this);
        s1.incrementWeight(STL_TRAVERSE_COST);
        s1.setBackMode(TraverseMode.LEG_SWITCH);
        return s1.makeState();
    }

    // anecdotally, the lower bound search is about 2x faster when you don't reach stops
    // and therefore don't even consider boarding
    @Override
    public double weightLowerBound(RoutingRequest options) {
        return options.transitAllowed() ? 0 : Double.POSITIVE_INFINITY;
    }
    
    @Override
    public Vertex getFromVertex() {
        return this.fromv;
    }
    
    @Override
    public Vertex getToVertex() {
        return this.tov;
    }
    
    @Override
    public Trip getTrip() {
        return null;
    }
    
    @Override
    public boolean isRoundabout() {
        return false;
    }
    
    @Override
    public String toString() {
        return "StreetTransitLink(" + this.fromv + " -> " + this.tov + ")";
    }
    
}
