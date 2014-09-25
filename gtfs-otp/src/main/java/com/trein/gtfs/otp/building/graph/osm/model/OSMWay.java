package com.trein.gtfs.otp.building.graph.osm.model;

import java.util.ArrayList;
import java.util.List;

public class OSMWay extends OSMWithTags {
    
    private final List<Long> _nodes = new ArrayList<Long>();
    
    public void addNodeRef(OSMNodeRef nodeRef) {
        this._nodes.add(nodeRef.getRef());
    }
    
    public void addNodeRef(long nodeRef) {
        this._nodes.add(nodeRef);
    }
    
    public List<Long> getNodeRefs() {
        return this._nodes;
    }
    
    @Override
    public String toString() {
        return "osm way " + this.id;
    }
    
    /**
     * Returns true if bicycle dismounts are forced.
     *
     * @return
     */
    public boolean isBicycleDismountForced() {
        String bicycle = getTag("bicycle");
        return isTag("cycleway", "dismount") || "dismount".equals(bicycle);
    }
    
    /**
     * Returns true if these are steps.
     *
     * @return
     */
    public boolean isSteps() {
        return "steps".equals(getTag("highway"));
    }
    
    /**
     * Is this way a roundabout?
     *
     * @return
     */
    public boolean isRoundabout() {
        return "roundabout".equals(getTag("junction"));
    }
    
    /**
     * Returns true if this is a one-way street for driving.
     *
     * @return
     */
    public boolean isOneWayForwardDriving() {
        return isTagTrue("oneway");
    }
    
    /**
     * Returns true if this way is one-way in the opposite direction of its definition.
     *
     * @return
     */
    public boolean isOneWayReverseDriving() {
        return isTag("oneway", "-1");
    }
    
    /**
     * Returns true if bikes can only go forward.
     *
     * @return
     */
    public boolean isOneWayForwardBicycle() {
        String oneWayBicycle = getTag("oneway:bicycle");
        return isTrue(oneWayBicycle) || isTagFalse("bicycle:backwards");
    }
    
    /**
     * Returns true if bikes can only go in the reverse direction.
     *
     * @return
     */
    public boolean isOneWayReverseBicycle() {
        return "-1".equals(getTag("oneway:bicycle"));
    }
    
    /**
     * Some cycleways allow contraflow biking.
     *
     * @return
     */
    public boolean isOpposableCycleway() {
        // any cycleway which is opposite* allows contraflow biking
        String cycleway = getTag("cycleway");
        String cyclewayLeft = getTag("cycleway:left");
        String cyclewayRight = getTag("cycleway:right");
        
        return ((cycleway != null) && cycleway.startsWith("opposite"))
                || ((cyclewayLeft != null) && cyclewayLeft.startsWith("opposite"))
                || ((cyclewayRight != null) && cyclewayRight.startsWith("opposite"));
    }
}
