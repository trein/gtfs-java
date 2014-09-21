package org.opentripplanner.standalone;


public class Sample {
    
    public final int t0, t1; // TODO change from times to distances.
    public final Vertex v0, v1;

    public Sample(Vertex v0, int t0, Vertex v1, int t1) {
        this.v0 = v0;
        this.t0 = t0;
        this.v1 = v1;
        this.t1 = t1;
    }
    
    public byte evalBoardings(ShortestPathTree spt) {
        State s0 = spt.getState(this.v0);
        State s1 = spt.getState(this.v1);
        int m0 = 255;
        int m1 = 255;
        if (s0 != null) {
            m0 = (s0.getNumBoardings());
        }
        if (s1 != null) {
            m1 = (s1.getNumBoardings());
        }
        return (byte) ((m0 < m1) ? m0 : m1);
    }

    public long eval(ShortestPathTree spt) {
        State s0 = spt.getState(this.v0);
        State s1 = spt.getState(this.v1);
        long m0 = Long.MAX_VALUE;
        long m1 = Long.MAX_VALUE;
        if (s0 != null) {
            m0 = (s0.getActiveTime() + this.t0);
        }
        if (s1 != null) {
            m1 = (s1.getActiveTime() + this.t1);
        }
        return (m0 < m1) ? m0 : m1;
    }
    
    /*
     * DUPLICATES code in sampleSet.eval(). should be deduplicated using a common function of
     * vertices/dists.
     */
    public long eval(TimeSurface surf) {
        int m0 = Integer.MAX_VALUE;
        int m1 = Integer.MAX_VALUE;
        if (this.v0 != null) {
            int s0 = surf.getTime(this.v0);
            if (s0 != TimeSurface.UNREACHABLE) {
                m0 = s0 + this.t0;
            }
        }
        if (this.v1 != null) {
            int s1 = surf.getTime(this.v1);
            if (s1 != TimeSurface.UNREACHABLE) {
                m1 = s1 + this.t1;
            }
        }
        return (m0 < m1) ? m0 : m1;
    }
    
    @Override
    public String toString() {
        return String.format("Sample: %s in %d sec or %s in %d sec\n", this.v0, this.t0, this.v1, this.t1);
    }

}
