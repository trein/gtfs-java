package org.opentripplanner.standalone;


/**
 * A stop associated with its elapsed time from a search location and the path for reaching it. Used
 * in profile routing. TODO rename this StopPath or something.
 */
public class StopAtDistance implements Comparable<StopAtDistance> {
    
    public StopCluster stop; // TODO rename to stopCluster, use StopCluster objects not strings?
    public TraverseMode mode;
    public int etime;
    public State state;
    
    /** @param state a state at a TransitStop */
    public StopAtDistance(State state) {
        this.state = state;
        this.etime = (int) state.getElapsedTimeSeconds();
        this.mode = state.getNonTransitMode(); // not sure if this is reliable, reset in caller.
        if (state.getVertex() instanceof TransitStop) {
            TransitStop tstop = (TransitStop) state.getVertex();
            this.stop = state.getOptions().rctx.graph.index.stopClusterForStop.get(tstop.getStop());
        }
    }
    
    @Override
    public int compareTo(StopAtDistance that) {
        return this.etime - that.etime;
    }
    
    @Override
    public String toString() {
        return String.format("stop cluster %s via mode %s at %d min", this.stop, this.mode, this.etime / 60);
    }
    
}
