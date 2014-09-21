package org.opentripplanner.standalone;

import java.util.List;
import java.util.Set;

/**
 * Abstract base class for edges in the (open)streetmap layer (might be paths, stairs, etc. as well
 * as streets). This can be used as a marker to detect edges in the street layer.
 */

/* package-private ? */
// EdgeWithElevation extends Edge
public abstract class StreetEdge extends EdgeWithElevation {
    
    private static final long serialVersionUID = 1L;
    public static final int CLASS_STREET = 3;
    public static final int CLASS_CROSSING = 4;
    public static final int CLASS_OTHERPATH = 5;
    public static final int CLASS_OTHER_PLATFORM = 8;
    public static final int CLASS_TRAIN_PLATFORM = 16;
    public static final int ANY_PLATFORM_MASK = 24;
    public static final int CROSSING_CLASS_MASK = 7; // ignore platform
    public static final int CLASS_LINK = 32; // on/offramps; OSM calls them "links"
    
    public StreetEdge(StreetVertex v1, StreetVertex v2) {
        super(v1, v2);
    }
    
    /**
     * Returns true if this RoutingRequest can traverse this edge.
     */
    public abstract boolean canTraverse(RoutingRequest options);
    
    public abstract boolean canTraverse(TraverseModeSet modes);
    
    public abstract String getLabel();
    
    public abstract double getLength();
    
    public abstract float getCarSpeed();
    
    public abstract void setCarSpeed(float carSpeed);
    
    public abstract int getInAngle();
    
    public abstract int getOutAngle();
    
    public abstract StreetTraversalPermission getPermission();
    
    public abstract boolean isNoThruTraffic();
    
    public abstract int getStreetClass();
    
    public abstract boolean isWheelchairAccessible();
    
    public abstract Set<Alert> getNotes();
    
    public abstract Set<Alert> getWheelchairNotes();
    
    public abstract List<TurnRestriction> getTurnRestrictions();
    
}
