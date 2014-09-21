package org.opentripplanner.standalone;

import java.io.Serializable;

/**
 * Interface for classes that provides an admissible estimate of (lower bound on) the weight of a
 * path to the target, starting from a given state.
 */
public interface RemainingWeightHeuristic extends Serializable {
    
    /**
     * Perform any one-time setup and pre-computation that will be needed by later calls to
     * computeForwardWeight/computeReverseWeight.
     */
    public void initialize(State s, Vertex target, long abortTime);
    
    public double computeForwardWeight(State s, Vertex target);
    
    public double computeReverseWeight(State s, Vertex target);

    /** Reset any cached data in the heuristic, e.g. between rounds of a retrying path service. */
    public void reset();

    /**
     * Call to cause the heuristic to perform some predetermined amount of work improving its
     * estimate. Avoids thread synchronization evil by interleaving forward and backward searches.
     */
    public void doSomeWork();

}

// Perhaps directionality should also be defined during the setup,
// instead of having two separate methods for the two directions.
// We might not even need a setup method if the routing options are just passed into the
// constructor.
