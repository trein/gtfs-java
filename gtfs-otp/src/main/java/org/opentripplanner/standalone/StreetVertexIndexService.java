package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface StreetVertexIndexService {
    
    /**
     * Returns the vertices intersecting with the specified envelope.
     *
     * @param envelope
     * @return
     */
    public Collection<Vertex> getVerticesForEnvelope(Envelope envelope);
    
    /**
     * Return the edges whose geometry intersect with the specified envelope. Warning: edges w/o
     * geometry will not be indexed.
     *
     * @param envelope
     * @return
     */
    public Collection<Edge> getEdgesForEnvelope(Envelope envelope);
    
    /**
     * Get the closest edges to this location are traversable given these preferences.
     *
     * @param location
     * @param prefs Must be able to traverse these edges given these preferences.
     * @param extraEdges Additional edges to consider, may be null
     * @param preferredEdges Edges which are preferred, may be null
     * @param possibleTransitLinksOnly Only include possible transit links.
     * @return
     */
    public CandidateEdgeBundle getClosestEdges(GenericLocation location, TraversalRequirements reqs, List<Edge> extraEdges,
            Collection<Edge> preferredEdges, boolean possibleTransitLinksOnly);
    
    /**
     * Get the closest edges to this location are traversable given these preferences. Convenience
     * wrapper for above.
     *
     * @param location
     * @param prefs
     * @return
     */
    public CandidateEdgeBundle getClosestEdges(GenericLocation location, TraversalRequirements reqs);
    
    public List<TransitStop> getNearbyTransitStops(Coordinate coordinate, double radius);
    
    public List<TransitStop> getNearbyTransitStops(Coordinate coordinateOne, Coordinate coordinateTwo);
    
    /**
     * Finds the appropriate vertex for this location.
     *
     * @param location
     * @param options
     * @return
     */
    Vertex getVertexForLocation(GenericLocation location, RoutingRequest options);
    
    /**
     * Finds the appropriate vertex for this location.
     *
     * @param place
     * @param options
     * @param other non-null when another vertex has already been found. Passed in so that any extra
     *        edges made when locating the previous vertex may be used to locate this one as well.
     * @return
     */
    Vertex getVertexForLocation(GenericLocation place, RoutingRequest options, Vertex other);
}
