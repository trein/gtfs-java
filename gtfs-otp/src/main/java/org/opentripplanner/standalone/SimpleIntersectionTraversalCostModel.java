package org.opentripplanner.standalone;


public class SimpleIntersectionTraversalCostModel extends AbstractIntersectionTraversalCostModel {
    
    // Model parameters are here. //
    // Constants for when there is a traffic light.
    
    /** Expected time it takes to make a right at a light. */
    private final Double expectedRightAtLightTimeSec = 15.0;
    
    /** Expected time it takes to continue straight at a light. */
    private final Double expectedStraightAtLightTimeSec = 15.0;
    
    /** Expected time it takes to turn left at a light. */
    private final Double expectedLeftAtLightTimeSec = 15.0;
    
    // Constants for when there is no traffic light
    
    /** Expected time it takes to make a right without a stop light. */
    private final Double expectedRightNoLightTimeSec = 8.0;
    
    /** Expected time it takes to continue straight without a stop light. */
    private final Double expectedStraightNoLightTimeSec = 5.0;
    
    /** Expected time it takes to turn left without a stop light. */
    private final Double expectedLeftNoLightTimeSec = 8.0;
    
    @Override
    public double computeTraversalCost(IntersectionVertex v, PlainStreetEdge from, PlainStreetEdge to, TraverseMode mode,
            RoutingRequest options, float fromSpeed, float toSpeed) {
        
        // If the vertex is free-flowing then (by definition) there is no cost to traverse it.
        if (v.inferredFreeFlowing()) { return 0; }
        
        // Non-driving cases are much simpler. Handled generically in the base class.
        if (!mode.isDriving()) { return computeNonDrivingTraversalCost(v, from, to, fromSpeed, toSpeed); }
        
        double turnCost = 0;
        
        int turnAngle = calculateTurnAngle(from, to, options);
        if (v.trafficLight) {
            // Use constants that apply when there are stop lights.
            if (isRightTurn(turnAngle)) {
                turnCost = this.expectedRightAtLightTimeSec;
            } else if (isLeftTurn(turnAngle)) {
                turnCost = this.expectedLeftAtLightTimeSec;
            } else {
                turnCost = this.expectedStraightAtLightTimeSec;
            }
        } else {
            
            // assume highway vertex
            if ((from.getCarSpeed() > 25) && (to.getCarSpeed() > 25)) { return 0; }
            
            // Use constants that apply when no stop lights.
            if (isRightTurn(turnAngle)) {
                turnCost = this.expectedRightNoLightTimeSec;
            } else if (isLeftTurn(turnAngle)) {
                turnCost = this.expectedLeftNoLightTimeSec;
            } else {
                turnCost = this.expectedStraightNoLightTimeSec;
            }
        }
        
        return turnCost;
    }
    
}
