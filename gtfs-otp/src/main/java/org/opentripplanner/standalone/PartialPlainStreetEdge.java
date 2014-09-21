package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Represents a sub-segment of a StreetEdge. TODO we need a way to make sure all temporary edges are
 * recorded as such and assigned a routingcontext when they are created. That list should probably
 * be in the routingContext itself instead of the created StreetLocation.
 */
public class PartialPlainStreetEdge extends PlainStreetEdge {
    
    private static final long serialVersionUID = 1L;
    
    public RoutingContext visibleTo = null;
    
    /**
     * The edge on which this lies.
     */
    private final StreetEdge parentEdge;
    
    public PartialPlainStreetEdge(StreetEdge parentEdge, StreetVertex v1, StreetVertex v2, LineString geometry, String name,
            double length, StreetTraversalPermission permission, boolean back) {
        super(v1, v2, geometry, name, length, permission, back, parentEdge.getCarSpeed());
        
        this.parentEdge = parentEdge;
    }

    /**
     * Simplifies construction by copying some stuff from the parentEdge.
     */
    public PartialPlainStreetEdge(StreetEdge parentEdge, StreetVertex v1, StreetVertex v2, LineString geometry, String name,
            double length) {
        this(parentEdge, v1, v2, geometry, name, length, parentEdge.getPermission(), false);
    }

    /**
     * Partial edges are always partial.
     */
    @Override
    public boolean isPartial() {
        return true;
    }

    /**
     * Have the ID of their parent.
     */
    @Override
    public int getId() {
        return this.parentEdge.getId();
    }

    /**
     * Have the inbound angle of their parent.
     */
    @Override
    public int getInAngle() {
        return this.parentEdge.getInAngle();
    }

    /**
     * Have the outbound angle of their parent.
     */
    @Override
    public int getOutAngle() {
        return this.parentEdge.getInAngle();
    }

    /**
     * This implementation makes it so that TurnRestrictions on the parent edge are applied to this
     * edge as well.
     */
    @Override
    public boolean isEquivalentTo(Edge e) {
        return ((e == this) || (e == this.parentEdge));
    }

    @Override
    public boolean isReverseOf(Edge e) {
        Edge other = e;
        if (e instanceof PartialPlainStreetEdge) {
            other = ((PartialPlainStreetEdge) e).parentEdge;
        }

        // TODO(flamholz): is there a case where a partial edge has a reverse of its own?
        return this.parentEdge.isReverseOf(other);
    }

    @Override
    public boolean isRoundabout() {
        return this.parentEdge.isRoundabout();
    }

    /**
     * Returns true if this edge is trivial - beginning and ending at the same point.
     */
    public boolean isTrivial() {
        Coordinate fromCoord = this.getFromVertex().getCoordinate();
        Coordinate toCoord = this.getToVertex().getCoordinate();
        return fromCoord.equals(toCoord);
    }

    @Override
    public String toString() {
        return "PartialPlainStreetEdge(" + this.getName() + ", " + this.getFromVertex() + " -> " + this.getToVertex()
                + " length=" + this.getLength() + " carSpeed=" + this.getCarSpeed() + " parentEdge=" + this.parentEdge + ")";
    }
    
    @Override
    public State traverse(State s0) {
        // Split edges should only be usable by the routing context that created them.
        // This should alleviate the concurrency problem in issue 1025.
        // In the window of time before the visibleTo field is set, traversal will also fail (which
        // is what we want).
        if (!(this.visibleTo == s0.getOptions().rctx)) { return null; }
        return super.traverse(s0);
    }
    
}
