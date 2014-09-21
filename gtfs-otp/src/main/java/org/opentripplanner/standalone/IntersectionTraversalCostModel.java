package org.opentripplanner.standalone;


/**
 * An interface to a model that computes the costs of turns. Turn costs are in units of seconds -
 * they represent the expected amount of time it would take to make a turn.
 *
 * @author avi
 */
public interface IntersectionTraversalCostModel {

    /**
     * Compute the cost of turning onto "to" from "from".
     *
     * @return expected number of seconds the traversal is expected to take.
     */
    public double computeTraversalCost(IntersectionVertex v, PlainStreetEdge from, PlainStreetEdge to, TraverseMode mode,
            RoutingRequest options, float fromSpeed, float toSpeed);
    
}
