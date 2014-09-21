package org.opentripplanner.standalone;


/**
 * The various getShortestPathTree methods may return a ShortestPathTree or null. Null indicates
 * that something went horribly wrong and no useful information could be produced. If an SPT is
 * returned but req.rctx.aborted is true, the SPT may contain some valid paths but any enclosing
 * retrying mechanism should end the search. If req.rctx.aborted is false the retrying mechanism may
 * attempt to get additional paths by altering parameters.
 */
public interface SPTService {

    /**
     * Generate a shortest path tree for this RoutingRequest.
     *
     * @param req
     * @return
     */
    public ShortestPathTree getShortestPathTree(RoutingRequest req);
    
    /**
     * Generate SPT, controlling the timeout externally.
     *
     * @param req
     * @param timeoutSeconds
     * @return
     */
    public ShortestPathTree getShortestPathTree(RoutingRequest req, double timeoutSeconds);
    
    /**
     * Find a shortest path tree and control when the search terminates.
     *
     * @param req
     * @param timeoutSeconds
     * @param terminationStrategy
     * @return
     */
    public ShortestPathTree getShortestPathTree(RoutingRequest req, double timeoutSeconds,
            SearchTerminationStrategy terminationStrategy);
    
}
