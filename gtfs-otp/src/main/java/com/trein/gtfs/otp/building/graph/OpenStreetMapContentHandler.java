package com.trein.gtfs.otp.building.graph;

import com.trein.gtfs.otp.building.graph.osm.model.OSMNode;
import com.trein.gtfs.otp.building.graph.osm.model.OSMRelation;
import com.trein.gtfs.otp.building.graph.osm.model.OSMWay;

/**
 * An interface to process/store parsed OpenStreetMap data.
 *
 * @see org.opentripplanner.openstreetmap.services.OpenStreetMapProvider
 */

public interface OpenStreetMapContentHandler {
    
    /**
     * Notifes the handler to expect the second stage of parsing (ie. nodes).
     */
    public void secondPhase();
    
    /**
     * Stores a node.
     */
    public void addNode(OSMNode node);
    
    /**
     * Stores a way.
     */
    public void addWay(OSMWay way);
    
    /**
     * Stores a relation.
     */
    public void addRelation(OSMRelation relation);
    
    /**
     * Called when the relation-processing phase is complete
     */
    public void doneRelations();
    
    /**
     * Called after the final phase, when all nodes are loaded
     */
    public void nodesLoaded();
}
