package org.opentripplanner.standalone;


/**
 * A vertex acting as a starting point for planning a trip while onboard an existing trip.
 *
 * @author laurent
 */
public class OnboardDepartVertex extends Vertex {
    
    private static final long serialVersionUID = -6721280275560962711L;
    
    public OnboardDepartVertex(String label, double lon, double lat) {
        // This vertex is *alway* temporary, so graph is always null.
        super(null, label, lon, lat, label);
    }
    
    @Override
    public int removeTemporaryEdges() {
        // We can remove all
        int nRemoved = 0;
        for (Edge e : getOutgoing()) {
            if (e.detach() != 0) {
                nRemoved += 1;
            }
        }
        if (!getIncoming().isEmpty()) { throw new AssertionError("Can't have incoming edge on a OnboardDepartVertex"); }
        return nRemoved;
    }
}
