package org.opentripplanner.standalone;


/**
 * Abstract turn cost model provides various methods most implementations will use.
 *
 * @author avi
 */
public abstract class AbstractIntersectionTraversalCostModel implements IntersectionTraversalCostModel {
    
    /**
     * Factor by which absolute turn angles are divided to get turn costs for non-driving scenarios.
     */
    protected Double nonDrivingTurnCostFactor = 1.0 / 20.0;
    
    protected Integer minRightTurnAngle = 45;

    protected Integer maxRightTurnAngle = 135;
    
    protected Integer minLeftTurnAngle = 225;

    protected Integer maxLeftTurnAngle = 315;
    
    /** Returns true if this angle represents a right turn. */
    protected boolean isRightTurn(int turnAngle) {
        return ((turnAngle >= this.minRightTurnAngle) && (turnAngle < this.maxRightTurnAngle));
    }
    
    /** Returns true if this angle represents a left turn. */
    protected boolean isLeftTurn(int turnAngle) {
        return ((turnAngle >= this.minLeftTurnAngle) && (turnAngle < this.maxLeftTurnAngle));
    }
    
    /**
     * Computes the turn cost in seconds for non-driving traversal modes. TODO(flamholz): this
     * should probably account for whether there is a traffic light?
     */
    protected double computeNonDrivingTraversalCost(IntersectionVertex v, PlainStreetEdge from, PlainStreetEdge to,
            float fromSpeed, float toSpeed) {
        int outAngle = to.getOutAngle();
        int inAngle = from.getInAngle();
        int turnCost = Math.abs(outAngle - inAngle);
        if (turnCost > 180) {
            turnCost = 360 - turnCost;
        }
        
        // NOTE: This makes the turn cost lower the faster you're going
        return (this.nonDrivingTurnCostFactor * turnCost) / toSpeed;
    }
    
    /**
     * Calculates the turn angle from the incoming/outgoing edges and routing request. Corrects for
     * the side of the street they are driving on.
     */
    protected int calculateTurnAngle(PlainStreetEdge from, PlainStreetEdge to, RoutingRequest options) {
        int angleOutOfIntersection = to.getInAngle();
        int angleIntoIntersection = from.getOutAngle();

        // Put out to the right of in; i.e. represent everything as one long right turn
        // Also ensures that turnAngle is always positive.
        if (angleOutOfIntersection < angleIntoIntersection) {
            angleOutOfIntersection += 360;
        }
        
        int turnAngle = angleOutOfIntersection - angleIntoIntersection;
        
        if (!options.driveOnRight) {
            turnAngle = 360 - turnAngle;
        }
        
        return turnAngle;
    }
    
    /* Concrete subclasses must implement this */
    @Override
    public abstract double computeTraversalCost(IntersectionVertex v, PlainStreetEdge from, PlainStreetEdge to,
            TraverseMode mode, RoutingRequest options, float fromSpeed, float toSpeed);
    
}
