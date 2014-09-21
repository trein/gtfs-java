package org.opentripplanner.standalone;


/**
 * A trivial heuristic that always returns 0, which is always admissible. For use in testing and
 * troubleshooting.
 *
 * @author andrewbyrd
 */
public class TrivialRemainingWeightHeuristic implements RemainingWeightHeuristic {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public void initialize(State s, Vertex target, long abortTime) {
    }
    
    @Override
    public double computeForwardWeight(State s, Vertex target) {
        return 0;
    }
    
    @Override
    public double computeReverseWeight(State s, Vertex target) {
        return 0;
    }
    
    /**
     * Factory that turns off goal-direction heuristics in OTP for comparison. results should be
     * identical when heuristics are switched off.
     */
    public static class Factory implements RemainingWeightHeuristicFactory {
        @Override
        public RemainingWeightHeuristic getInstanceForSearch(RoutingRequest opt) {
            return new TrivialRemainingWeightHeuristic();
        }
    }
    
    @Override
    public void reset() {
    }

    @Override
    public void doSomeWork() {
    }
}
