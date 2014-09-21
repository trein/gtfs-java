package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/** A response object describing a non-transit part of an option (usually access/egress). */
public class StreetSegment {
    
    private static final Logger LOG = LoggerFactory.getLogger(StreetSegment.class);
    
    public TraverseMode mode;
    public int time;
    public EncodedPolylineBean geometry;
    public List<WalkStep> walkSteps = Lists.newArrayList();
    
    /**
     * Build the walksteps from the final State of a path. The path may contain more than one leg,
     * because you may need to walk the bike. Therefore accumulate the geometries into one long
     * polyline, and accumulate the walksteps.
     */
    public StreetSegment(State state) {
        GraphPath path = new GraphPath(state, false);
        CoordinateArrayListSequence coordinates = new CoordinateArrayListSequence();
        for (Edge edge : path.edges) {
            LineString geometry = edge.getGeometry();
            if (geometry != null) {
                if (coordinates.size() == 0) {
                    coordinates.extend(geometry.getCoordinates());
                } else {
                    coordinates.extend(geometry.getCoordinates(), 1); // Avoid duplications
                }
            }
        }
        Geometry geom = GeometryUtils.getGeometryFactory().createLineString(coordinates);
        this.geometry = PolylineEncoder.createEncodings(geom);
        PlanGenerator pgen = new PlanGenerator(null, null);
        Itinerary itin = pgen.generateItinerary(path, false);
        for (Leg leg : itin.legs) {
            this.walkSteps.addAll(leg.walkSteps);
        }
        this.time = (int) (state.getElapsedTimeSeconds());
    }
    
    /**
     * A StreetSegment is very similar to a StopAtDistance but it's a response object so the State
     * has to be rendered into walksteps.
     */
    public StreetSegment(StopAtDistance sd) {
        this(sd.state);
        this.mode = sd.mode; // Intended mode is known more reliably in a StopAtDistance than from a
                             // State.
    }
    
    /** Make a collections of StreetSegments from a collection of StopAtDistance. */
    public static List<StreetSegment> list(Collection<StopAtDistance> sds) {
        if ((sds == null) || sds.isEmpty()) { return null; }
        List<StreetSegment> ret = Lists.newArrayList();
        for (StopAtDistance sd : sds) {
            ret.add(new StreetSegment(sd));
        }
        return ret;
    }
    
}
