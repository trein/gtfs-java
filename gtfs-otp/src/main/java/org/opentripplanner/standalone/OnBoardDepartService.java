package org.opentripplanner.standalone;

import java.io.Serializable;

/**
 * Graph service for depart-on-board mode.
 *
 * @author laurent
 */
public interface OnBoardDepartService extends Serializable {
    
    public abstract Vertex setupDepartOnBoard(RoutingContext ctx);
}
