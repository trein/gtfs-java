package org.opentripplanner.standalone;

import java.util.List;

public interface PathService {
    
    public List<GraphPath> getPaths(RoutingRequest options);
    
    /**
     * In the case of "arrive-by" routing, the origin state is actually the user's end location and
     * the target vertex is the user's start location.
     */
    
    /**
     * TODO: there was a separate method to handle intermediates; now the pathservice should just
     * figure this out from the request. Here we wish to plan a trip that starts at "fromPlace",
     * travels through the intermediate places in some arbitrary but hopefully optimal order, and
     * eventually end up at "toPlace".
     */
    
}
