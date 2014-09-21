package org.opentripplanner.standalone;

import org.onebusaway.gtfs.model.Stop;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * A transit vehicle's journey between departure at one stop and arrival at the next. This version
 * represents a set of such journeys specified by a TripPattern.
 */
public class PatternHop extends TablePatternEdge implements OnboardEdge, HopEdge {
    
    private static final long serialVersionUID = 1L;
    
    private final Stop begin, end;
    
    public int stopIndex;
    
    private LineString geometry = null;
    
    public PatternHop(PatternStopVertex from, PatternStopVertex to, Stop begin, Stop end, int stopIndex) {
        super(from, to);
        this.begin = begin;
        this.end = end;
        this.stopIndex = stopIndex;
        getPattern().setPatternHop(stopIndex, this);
    }
    
    @Override
    public double getDistance() {
        return SphericalDistanceLibrary.getInstance().distance(this.begin.getLat(), this.begin.getLon(), this.end.getLat(),
                this.end.getLon());
    }
    
    public TraverseMode getMode() {
        return GtfsLibrary.getTraverseMode(getPattern().route);
    }

    @Override
    public String getName() {
        return GtfsLibrary.getRouteName(getPattern().route);
    }

    @Override
    public State optimisticTraverse(State state0) {
        RoutingRequest options = state0.getOptions();

        // Ignore this edge if either of its stop is banned hard
        if (!options.bannedStopsHard.isEmpty()) {
            if (options.bannedStopsHard.matches(((PatternStopVertex) this.fromv).getStop())
                    || options.bannedStopsHard.matches(((PatternStopVertex) this.tov).getStop())) { return null; }
        }
        
        int runningTime = getPattern().scheduledTimetable.getBestRunningTime(this.stopIndex);
        StateEditor s1 = state0.edit(this);
        s1.incrementTimeInSeconds(runningTime);
        s1.setBackMode(getMode());
        s1.incrementWeight(runningTime);
        return s1.makeState();
    }
    
    @Override
    public double timeLowerBound(RoutingRequest options) {
        return getPattern().scheduledTimetable.getBestRunningTime(this.stopIndex);
    }

    @Override
    public double weightLowerBound(RoutingRequest options) {
        return timeLowerBound(options);
    }

    @Override
    public State traverse(State s0) {
        RoutingRequest options = s0.getOptions();

        // Ignore this edge if either of its stop is banned hard
        if (!options.bannedStopsHard.isEmpty()) {
            if (options.bannedStopsHard.matches(((PatternStopVertex) this.fromv).getStop())
                    || options.bannedStopsHard.matches(((PatternStopVertex) this.tov).getStop())) { return null; }
        }

        TripTimes tripTimes = s0.getTripTimes();
        int runningTime = tripTimes.getRunningTime(this.stopIndex);
        StateEditor s1 = s0.edit(this);
        s1.incrementTimeInSeconds(runningTime);
        if (s0.getOptions().arriveBy) {
            s1.setZone(getBeginStop().getZoneId());
        } else {
            s1.setZone(getEndStop().getZoneId());
        }
        // s1.setRoute(pattern.getExemplar().route.getId());
        s1.incrementWeight(runningTime);
        s1.setBackMode(getMode());
        return s1.makeState();
    }
    
    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }
    
    @Override
    public LineString getGeometry() {
        if (this.geometry == null) {
            
            Coordinate c1 = new Coordinate(this.begin.getLon(), this.begin.getLat());
            Coordinate c2 = new Coordinate(this.end.getLon(), this.end.getLat());
            
            this.geometry = GeometryUtils.getGeometryFactory().createLineString(new Coordinate[] { c1, c2 });
        }
        return this.geometry;
    }
    
    @Override
    public Stop getEndStop() {
        return this.end;
    }
    
    @Override
    public Stop getBeginStop() {
        return this.begin;
    }
    
    @Override
    public String toString() {
        return "PatternHop(" + getFromVertex() + ", " + getToVertex() + ")";
    }
    
    @Override
    public int getStopIndex() {
        return this.stopIndex;
    }
}
