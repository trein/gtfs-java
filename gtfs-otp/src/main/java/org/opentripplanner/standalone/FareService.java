package org.opentripplanner.standalone;


/**
 * Computes a fare for a given GraphPath.
 * 
 * @author novalis
 */
public interface FareService {
    public Fare getCost(GraphPath path);
}
