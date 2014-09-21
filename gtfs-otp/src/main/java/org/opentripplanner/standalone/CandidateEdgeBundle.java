package org.opentripplanner.standalone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CandidateEdgeBundle extends ArrayList<CandidateEdge> {
    private static final long serialVersionUID = 20120222L;
    
    // maximum difference in distance for two geometries to be considered coincident
    public static final double DISTANCE_ERROR = 0.000001;
    
    private static final double DIRECTION_ERROR = 0.05;
    
    public StreetVertex endwiseVertex = null;
    
    public CandidateEdge best = null;
    
    @Override
    public boolean add(CandidateEdge ce) {
        if ((this.best == null) || (ce.score < this.best.score)) {
            this.endwiseVertex = ce.endwiseVertex;
            this.best = ce;
        }
        return super.add(ce);
    }
    
    public List<StreetEdge> toEdgeList() {
        List<StreetEdge> ret = new ArrayList<StreetEdge>();
        for (CandidateEdge ce : this) {
            ret.add(ce.edge);
        }
        return ret;
    }
    
    static class DistanceAndAngle {
        double distance;
        
        double angle;
        
        boolean endwise;
        
        public DistanceAndAngle(double distance, double angle, boolean endwise) {
            this.distance = distance;
            this.angle = angle;
            this.endwise = endwise;
        }
    }
    
    public Collection<CandidateEdgeBundle> binByDistanceAndAngle() {
        // Map of from distance, angle pairs to bundles of edges.
        Map<DistanceAndAngle, CandidateEdgeBundle> bins = new HashMap<DistanceAndAngle, CandidateEdgeBundle>();
        CANDIDATE: for (CandidateEdge ce : this) {
            for (Entry<DistanceAndAngle, CandidateEdgeBundle> bin : bins.entrySet()) {
                double distance = bin.getKey().distance;
                double direction = bin.getKey().angle;
                if ((Math.abs(direction - ce.directionToEdge) < DIRECTION_ERROR)
                        && (Math.abs(distance - ce.distance) < DISTANCE_ERROR) && (ce.endwise() == bin.getKey().endwise)) {
                    bin.getValue().add(ce);
                    continue CANDIDATE;
                }
            }
            DistanceAndAngle rTheta = new DistanceAndAngle(ce.distance, ce.directionToEdge, ce.endwise());
            CandidateEdgeBundle bundle = new CandidateEdgeBundle();
            bundle.add(ce);
            bins.put(rTheta, bundle);
        }
        return bins.values();
    }
    
    public boolean endwise() {
        return this.endwiseVertex != null;
    }
    
    public double getScore() {
        return this.best.score;
    }
    
    public boolean isPlatform() {
        for (CandidateEdge ce : CandidateEdgeBundle.this) {
            StreetEdge e = ce.edge;
            if ((e.getStreetClass() & StreetEdge.ANY_PLATFORM_MASK) != 0) { return true; }
        }
        return false;
    }
    
    public boolean allowsCars() {
        for (CandidateEdge ce : CandidateEdgeBundle.this) {
            StreetEdge e = ce.edge;
            if (e.getPermission().allows(StreetTraversalPermission.CAR)) { return true; }
        }
        return false;
    }
}
