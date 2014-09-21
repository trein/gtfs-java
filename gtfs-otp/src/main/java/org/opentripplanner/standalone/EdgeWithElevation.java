package org.opentripplanner.standalone;

/**
 * An edge which has an elevation profile -- a street, basically.
 */
public abstract class EdgeWithElevation extends Edge {
    private static final long serialVersionUID = 4603374694558661207L;

    public EdgeWithElevation(Vertex fromv, Vertex tov) {
        super(fromv, tov);
    }

    public abstract PackedCoordinateSequence getElevationProfile();
    
    public abstract PackedCoordinateSequence getElevationProfile(double from, double to);
    
    public abstract boolean setElevationProfile(PackedCoordinateSequence elevPCS, boolean computed);
    
    public abstract ElevationProfileSegment getElevationProfileSegment();
    
    public abstract boolean isElevationFlattened();
}
