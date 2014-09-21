package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * A transit vehicle's journey (temporary vertex) between departure while onboard a trip and arrival
 * at the next. This version represents a set of such journeys specified by a TripPattern.
 *
 * @author laurent
 */
public class OnBoardDepartPatternHop extends Edge implements OnboardEdge {
    private static final long serialVersionUID = 1L;
    
    private final TripTimes tripTimes;
    
    private final ServiceDay serviceDay;
    
    private final int stopIndex;
    
    private final double positionInHop;
    
    private final Trip trip;
    
    private final Stop endStop;
    
    private LineString geometry = null;
    
    /**
     * @param from Originating vertex.
     * @param to Destination vertex: a PatternStopVertex for the next stop of the current hop.
     * @param tripTimes Resolved trip times for the trip with updated real-time info if available.
     * @param serviceDay Service day on which trip is running.
     * @param stopIndex Index of the current stop.
     * @param positionInHop Between 0 to 1, an estimation of the covered distance in this hop so
     *        far.
     */
    public OnBoardDepartPatternHop(OnboardDepartVertex from, PatternStopVertex to, TripTimes tripTimes, ServiceDay serviceDay,
            int stopIndex, double positionInHop) {
        super(from, to);
        this.stopIndex = stopIndex;
        this.serviceDay = serviceDay;
        this.tripTimes = tripTimes;
        this.positionInHop = positionInHop;
        this.trip = tripTimes.trip;
        this.endStop = to.getStop();
    }
    
    @Override
    public double getDistance() {
        /*
         * Do not multiply by positionInHop, as it is already taken into account by the from vertex
         * location.
         */
        return SphericalDistanceLibrary.getInstance().distance(getFromVertex().getY(), getFromVertex().getX(),
                this.endStop.getLat(), this.endStop.getLon());
    }
    
    public TraverseMode getMode() {
        return GtfsLibrary.getTraverseMode(this.trip.getRoute());
    }
    
    @Override
    public String getName() {
        return GtfsLibrary.getRouteName(this.trip.getRoute());
    }
    
    @Override
    public State optimisticTraverse(State state0) {
        return traverse(state0);
    }
    
    @Override
    public State traverse(State state0) {
        RoutingRequest options = state0.getOptions();
        
        if (options.reverseOptimizing || options.reverseOptimizeOnTheFly) { throw new UnsupportedOperationException(
                "Cannot (yet) reverse-optimize depart-on-board mode."); }

        /* Can't be traversed backwards. */
        if (options.arriveBy) { return null; }
        
        StateEditor s1 = state0.edit(this);
        // s1.setBackMode(TraverseMode.BOARDING); TODO Do we need this?
        s1.setServiceDay(this.serviceDay);
        s1.setTripTimes(this.tripTimes);
        
        // s1.incrementNumBoardings(); TODO Needed?
        s1.setTripId(this.trip.getId());
        s1.setPreviousTrip(this.trip);
        s1.setZone(this.endStop.getZoneId());
        s1.setRoute(this.trip.getRoute().getId());
        
        int remainingTime = (int) Math.round((1.0 - this.positionInHop) * this.tripTimes.getRunningTime(this.stopIndex));
        
        s1.incrementTimeInSeconds(remainingTime);
        s1.incrementWeight(remainingTime);
        s1.setBackMode(getMode());
        s1.setEverBoarded(true);
        return s1.makeState();
    }
    
    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }
    
    @Override
    public LineString getGeometry() {
        if (this.geometry == null) {
            Coordinate c1 = new Coordinate(getFromVertex().getX(), getFromVertex().getY());
            Coordinate c2 = new Coordinate(this.endStop.getLon(), this.endStop.getLat());
            this.geometry = GeometryUtils.getGeometryFactory().createLineString(new Coordinate[] { c1, c2 });
        }
        return this.geometry;
    }
    
    @Override
    public String toString() {
        return "OnBoardPatternHop(" + getFromVertex() + ", " + getToVertex() + ")";
    }
    
    @Override
    public int getStopIndex() {
        return this.stopIndex;
    }
    
    @Override
    public Trip getTrip() {
        return this.trip;
    }
    
    @Override
    public String getDirection() {
        return this.tripTimes.getHeadsign(this.stopIndex);
    }
    
}
