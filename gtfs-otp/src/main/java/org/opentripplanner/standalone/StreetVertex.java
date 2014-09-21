package org.opentripplanner.standalone;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Abstract base class for vertices in the street layer of the graph. This includes both vertices
 * representing intersections or points (IntersectionVertices) and Elevator*Vertices.
 */
public abstract class StreetVertex extends Vertex {
    
    private static final long serialVersionUID = 1L;
    
    public StreetVertex(Graph g, String label, Coordinate coord, String streetName) {
        this(g, label, coord.x, coord.y, streetName);
    }
    
    public StreetVertex(Graph g, String label, double x, double y, String streetName) {
        super(g, label, x, y, streetName);
    }

}
