package org.opentripplanner.standalone;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;

public class CandidateEdge {
    private static final double PLATFORM_PREFERENCE = 2.0;
    
    private static final double SIDEWALK_PREFERENCE = 1.5;
    
    // Massive preference for streets that allow cars, also applied to platforms for vehicles of
    // the specified mode.
    private static final double CAR_PREFERENCE = 100;
    
    private static final double MAX_DIRECTION_DIFFERENCE = 180.0;
    
    private static final double MAX_ABS_DIRECTION_DIFFERENCE = 360.0;
    
    /** The edge being considered for linking. */
    public final StreetEdge edge;
    
    /** Pointer to the geometry (coordinate sequence) of the edge. */
    private final CoordinateSequence edgeCoords;
    
    /** Number of coordinates on the edge. */
    private final int numEdgeCoords;
    
    /** Whether point is located at a platform. */
    private final int platform;
    
    /** Preference value passed in. */
    private final double preference;
    
    /** Index of the closest segment along the edge. */
    private int nearestSegmentIndex;
    
    /** Fractional distance along the closest segment. */
    private double nearestSegmentFraction;
    
    /**
     * Set when to the closest endpoint of the edge when the input location is really sitting on
     * that endpoint (within some tolerance).
     */
    protected StreetVertex endwiseVertex;
    
    /** The coordinate of the nearest point on the edge to the linking location. */
    public Coordinate nearestPointOnEdge;
    
    /** Heading if given. */
    protected Double heading;
    
    /** Azimuth between input point and closest point on edge. */
    protected double directionToEdge;
    
    /** Azimuth of the subsegment of the edge to which the input point is closest. */
    protected double directionOfEdge;
    
    /**
     * Difference in direction between heading and nearest subsegment of edge. Null if no heading
     * given.
     */
    protected Double directionDifference;
    
    /** Distance from edge to linking point. */
    public double distance;
    
    /** Score of the match. Lower is better. */
    public double score;
    
    /** Sorts CandidateEdges by best score first (lower = better). */
    public static class CandidateEdgeScoreComparator implements Comparator<CandidateEdge> {
        @Override
        public int compare(CandidateEdge arg0, CandidateEdge arg1) {
            double score1 = arg0.score;
            double score2 = arg1.score;
            if (score1 == score2) {
                return 0;
            } else if (score1 < score2) { return -1; }
            return 1;
        }
    }

    /**
     * Construct CandidateEdge based on a GenericLocation. The edge's score is calculated as the
     * final step of construction.
     */
    public CandidateEdge(StreetEdge e, GenericLocation loc, double pref, TraverseModeSet mode) {
        this.preference = pref;
        this.edge = e;
        this.edgeCoords = e.getGeometry().getCoordinateSequence();
        this.numEdgeCoords = this.edgeCoords.size();
        this.platform = calcPlatform(mode);
        this.nearestPointOnEdge = new Coordinate();
        
        // Initializes nearestPointOnEdge, nearestSegmentIndex,
        // nearestSegmentFraction.
        this.distance = calcNearestPoint(loc.getCoordinate());
        
        // Calculates the endwise vertex as appropriate.
        this.endwiseVertex = calcEndwiseVertex();
        
        // Calculate the directional info.
        int edgeSegmentIndex = this.nearestSegmentIndex;
        Coordinate c0 = this.edgeCoords.getCoordinate(edgeSegmentIndex);
        Coordinate c1 = this.edgeCoords.getCoordinate(edgeSegmentIndex + 1);
        this.directionOfEdge = DirectionUtils.getAzimuth(c0, c1);
        this.directionToEdge = DirectionUtils.getAzimuth(this.nearestPointOnEdge, loc.getCoordinate());
        
        // Calculates the direction differently depending on whether a heading
        // is supplied.
        this.heading = loc.heading;
        if (this.heading != null) {
            double absDiff = Math.abs(this.heading - this.directionOfEdge);
            this.directionDifference = Math.min(MAX_ABS_DIRECTION_DIFFERENCE - absDiff, absDiff);
        }
        
        // Calculate the score last so it can use all other data.
        this.score = calcScore();
    }
    
    /** Construct CandidateEdge based on a Coordinate. */
    public CandidateEdge(StreetEdge e, Coordinate p, double pref, TraverseModeSet mode) {
        this(e, new GenericLocation(p), pref, mode);
    }
    
    public boolean endwise() {
        return this.endwiseVertex != null;
    }
    
    @Override
    public String toString() {
        return String.format(
                "CandidateEdge<edge=\"%s\" score=\"%f\" heading=\"%s\" directionDifference=\"%s\" nearestPoint=\"%s\">",
                this.edge, this.score, this.heading, this.directionDifference, this.nearestPointOnEdge);
    }
    
    /* PRIVATE METHODS */
    
    /** Initializes this.nearestPointOnEdge and other distance-related variables. */
    private double calcNearestPoint(Coordinate p) {
        LineString edgeGeom = this.edge.getGeometry();
        CoordinateSequence coordSeq = edgeGeom.getCoordinateSequence();
        int bestSeg = 0;
        double bestDist2 = Double.POSITIVE_INFINITY;
        double bestFrac = 0;
        double xscale = Math.cos((p.y * Math.PI) / 180);
        for (int seg = 0; seg < (this.numEdgeCoords - 1); seg++) {
            double x0 = coordSeq.getX(seg);
            double y0 = coordSeq.getY(seg);
            double x1 = coordSeq.getX(seg + 1);
            double y1 = coordSeq.getY(seg + 1);
            double frac = GeometryUtils.segmentFraction(x0, y0, x1, y1, p.x, p.y, xscale);
            // project to get closest point
            double x = x0 + (frac * (x1 - x0));
            double y = y0 + (frac * (y1 - y0));
            // find ersatz distance to edge (do not take root)
            double dx = (x - p.x) * xscale;
            double dy = y - p.y;
            double dist2 = (dx * dx) + (dy * dy);
            // replace best segments
            if (dist2 < bestDist2) {
                this.nearestPointOnEdge.x = x;
                this.nearestPointOnEdge.y = y;
                bestFrac = frac;
                bestSeg = seg;
                bestDist2 = dist2;
            }
        } // end loop over segments
        
        this.nearestSegmentIndex = bestSeg;
        this.nearestSegmentFraction = bestFrac;
        return Math.sqrt(bestDist2); // distanceLibrary.distance(p,
        // nearestPointOnEdge);
    }
    
    /** Calculates the endwiseVertex if appropriate. */
    private StreetVertex calcEndwiseVertex() {
        StreetVertex retV = null;
        if ((this.nearestSegmentIndex == 0) && (Math.abs(this.nearestSegmentFraction) < 0.000001)) {
            retV = (StreetVertex) this.edge.getFromVertex();
        } else if ((this.nearestSegmentIndex == (this.numEdgeCoords - 2))
                && (Math.abs(this.nearestSegmentFraction - 1.0) < 0.000001)) {
            retV = (StreetVertex) this.edge.getToVertex();
        }
        return retV;
    }
    
    /**
     * Get the platform mask for the given mode. This is compatible with the bit flags from
     * StreetEdge.getStreetClass().
     */
    private int calcPlatform(TraverseModeSet mode) {
        int out = 0;
        if (mode.getTrainish()) {
            out |= StreetEdge.CLASS_TRAIN_PLATFORM;
        }
        if (mode.getBusish()) {
            // includes CABLE_CAR
            out |= StreetEdge.CLASS_OTHER_PLATFORM;
        }
        return out;
    }
    
    /** Internal calculator for the score. Assumes that edge, platform and distance are initialized. */
    private double calcScore() {
        double myScore = 0;
        // why is this being scaled by 1/360th of the radius of the earth?
        myScore = (this.distance * SphericalDistanceLibrary.RADIUS_OF_EARTH_IN_M) / 360.0;
        myScore /= this.preference;
        if ((this.edge.getStreetClass() & this.platform) != 0) {
            // this a hack, but there's not really a better way to do it
            myScore /= PLATFORM_PREFERENCE;
        }
        if (this.edge.getName().contains("sidewalk")) {
            // this is a hack, but there's not really a better way to do it
            myScore /= SIDEWALK_PREFERENCE;
        }
        // apply strong preference to car edges and to platforms for the specified modes
        if (this.edge.getPermission().allows(StreetTraversalPermission.CAR)
                || ((this.edge.getStreetClass() & this.platform) != 0)) {
            // we're subtracting here because no matter how close we are to a
            // good non-car non-platform edge, we really want to avoid it in
            // case it's a Pedway or other weird and unlikely starting location.
            myScore -= CAR_PREFERENCE;
        }
        
        // Consider the heading in the score if it is available.
        if (this.heading != null) {
            // If you are moving along the edge, score is not penalized.
            // If you are moving against the edge, score is penalized by 1.
            myScore += (this.directionDifference / MAX_DIRECTION_DIFFERENCE);
        }
        
        // break ties by choosing shorter edges; this should cause split streets
        // to be preferred
        myScore += this.edge.getLength() / 1000000;
        return myScore;
    }
}
