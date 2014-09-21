package org.opentripplanner.standalone;

/**
 * Strategy interface to provide additional logic to decide if a given search should be terminated.
 *
 * @author bdferris
 */
public interface SearchTerminationStrategy {
    
    /**
     * @param origin the origin vertex
     * @param target the target vertex, may be null in an undirected search
     * @param current the current shortest path tree vertex
     * @param spt the current shortest path tree
     * @param traverseOptions the traverse options
     * @return true if the specified search should be terminated
     */
    public boolean shouldSearchContinue(Vertex origin, Vertex target, State current, ShortestPathTree spt,
            RoutingRequest traverseOptions);
}
