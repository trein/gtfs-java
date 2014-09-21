package org.opentripplanner.standalone;

/**
 * An interface for classes which produce RemainingWeightHeuristic instances specific to a given
 * path search, taking the TraverseOptions, transport modes, target vertex, etc. into account.
 *
 * @author andrewbyrd
 */
public interface RemainingWeightHeuristicFactory {
    
    public RemainingWeightHeuristic getInstanceForSearch(RoutingRequest opt);
    
}
