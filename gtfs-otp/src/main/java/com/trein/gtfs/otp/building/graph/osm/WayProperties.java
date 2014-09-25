package com.trein.gtfs.otp.building.graph.osm;

import com.trein.gtfs.otp.building.graph.osm.model.P2;

/**
 * Parameters applied to OSM ways, usually based on their tags: - Which modes can traverse it -
 * Dangerousness on a bicycle in both directions (OSM ways can be bidirectional).
 *
 * @author novalis
 */
public class WayProperties implements Cloneable {
    
    private StreetTraversalPermission permission;
    
    /**
     * A multiplicative parameter expressing how much less safe this way is than the default, in
     * terms of something like DALYs lost per meter. The first element safety in the direction of
     * the way and the second is safety in the opposite direction. TODO change all these identifiers
     * so it's clear that this only applies to bicycles. TODO change the identifiers to make it
     * clear that this reflects danger, not safety. TODO I believe the weights are rescaled later in
     * graph building to be >= 1, but verify.
     */
    private static final P2<Double> defaultSafetyFeatures = new P2<Double>(1.0, 1.0);
    
    private P2<Double> safetyFeatures = defaultSafetyFeatures;
    
    public void setSafetyFeatures(P2<Double> safetyFeatures) {
        this.safetyFeatures = safetyFeatures;
    }
    
    public P2<Double> getSafetyFeatures() {
        return this.safetyFeatures;
    }
    
    public void setPermission(StreetTraversalPermission permission) {
        this.permission = permission;
    }
    
    public StreetTraversalPermission getPermission() {
        return this.permission;
    }
    
    @Override
    public WayProperties clone() {
        WayProperties result;
        try {
            result = (WayProperties) super.clone();
            result.setSafetyFeatures(new P2<Double>(this.safetyFeatures.getFirst(), this.safetyFeatures.getSecond()));
            return result;
        } catch (CloneNotSupportedException e) {
            // unreached
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof WayProperties) {
            WayProperties other = (WayProperties) o;
            return this.safetyFeatures.equals(other.safetyFeatures) && (this.permission == other.permission);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.safetyFeatures.hashCode() + this.permission.hashCode();
    }
}
