package org.opentripplanner.standalone;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class is an helper for Edges and Vertexes to store various data about elevation profiles.
 */
public class ElevationProfileSegment implements Serializable {
    
    private static final long serialVersionUID = MavenVersion.VERSION.getUID();
    
    private static final Logger LOG = LoggerFactory.getLogger(ElevationProfileSegment.class);
    
    private PackedCoordinateSequence elevationProfile;
    
    private double length;
    
    private double slopeSpeedEffectiveLength;
    
    private double bicycleSafetyEffectiveLength;
    
    private double slopeWorkCost;
    
    private double maxSlope;
    
    protected boolean slopeOverride;
    
    private boolean flattened;
    
    public ElevationProfileSegment(double length) {
        this.length = length;
        this.slopeSpeedEffectiveLength = length;
        this.bicycleSafetyEffectiveLength = length;
        this.slopeWorkCost = length;
    }
    
    public double getMaxSlope() {
        return this.maxSlope;
    }
    
    public void setSlopeOverride(boolean slopeOverride) {
        this.slopeOverride = slopeOverride;
    }
    
    public boolean getSlopeOverride() {
        return this.slopeOverride;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public double getLength() {
        return this.length;
    }
    
    public void setSlopeSpeedEffectiveLength(double slopeSpeedEffectiveLength) {
        this.slopeSpeedEffectiveLength = slopeSpeedEffectiveLength;
    }
    
    public double getSlopeSpeedEffectiveLength() {
        return this.slopeSpeedEffectiveLength;
    }
    
    // TODO Do we really want to be using "effective lengths" instead of just edge weights?
    public void setBicycleSafetyEffectiveLength(double bicycleSafetyEffectiveLength) {
        this.bicycleSafetyEffectiveLength = bicycleSafetyEffectiveLength;
    }
    
    public double getBicycleSafetyEffectiveLength() {
        return this.bicycleSafetyEffectiveLength;
    }
    
    public void setSlopeWorkCost(double slopeWorkCost) {
        this.slopeWorkCost = slopeWorkCost;
    }
    
    public double getSlopeWorkCost() {
        return this.slopeWorkCost;
    }
    
    public PackedCoordinateSequence getElevationProfile() {
        return this.elevationProfile;
    }
    
    public PackedCoordinateSequence getElevationProfile(double start, double end) {
        return ElevationUtils.getPartialElevationProfile(this.elevationProfile, start, end);
    }
    
    public void setElevationProfile(PackedCoordinateSequence elevationProfile) {
        this.elevationProfile = elevationProfile;
    }
    
    public boolean setElevationProfile(PackedCoordinateSequence elev, boolean computed, boolean slopeLimit) {
        if ((elev == null) || (elev.size() < 2)) { return false; }
        
        if (this.slopeOverride && !computed) { return false; }
        
        this.elevationProfile = elev;
        
        // compute the various costs of the elevation changes
        double lengthMultiplier = ElevationUtils.getLengthMultiplierFromElevation(elev);
        if (Double.isNaN(lengthMultiplier)) {
            LOG.error("lengthMultiplier from elevation profile is NaN, setting to 1");
            lengthMultiplier = 1;
        }
        
        this.length *= lengthMultiplier;
        this.bicycleSafetyEffectiveLength *= lengthMultiplier;
        
        SlopeCosts costs = ElevationUtils.getSlopeCosts(elev, slopeLimit);
        this.slopeSpeedEffectiveLength = costs.slopeSpeedEffectiveLength;
        this.maxSlope = costs.maxSlope;
        this.slopeWorkCost = costs.slopeWorkCost;
        this.bicycleSafetyEffectiveLength += costs.slopeSafetyCost;
        this.flattened = costs.flattened;
        
        return costs.flattened;
    }
    
    @Override
    public String toString() {
        String out = "";
        if ((this.elevationProfile == null) || (this.elevationProfile.size() == 0)) { return "(empty elevation profile)"; }
        for (int i = 0; i < this.elevationProfile.size(); ++i) {
            Coordinate coord = this.elevationProfile.getCoordinate(i);
            out += "(" + coord.x + "," + coord.y + "), ";
        }
        return out.substring(0, out.length() - 2);
    }
    
    public boolean isFlattened() {
        return this.flattened;
    }
}
