package org.opentripplanner.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This trivial RemainingWeightHeuristicFactory returns a Euclidean heuristic instance for every
 * search, irrespective of destination, modes, etc.
 *
 * @author andrewbyrd
 */
public class DefaultRemainingWeightHeuristicFactoryImpl implements RemainingWeightHeuristicFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRemainingWeightHeuristicFactoryImpl.class);
    
    @Override
    public RemainingWeightHeuristic getInstanceForSearch(RoutingRequest opt) {
        // LOG.debug("Using Euclidean heuristic independent of search type.");
        return new DefaultRemainingWeightHeuristic();
    }
    
}
