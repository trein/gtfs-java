package org.opentripplanner.standalone;

import java.util.HashMap;
import java.util.List;

public interface GraphBuilder {
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra);
    
    /**
     * An set of ids which identifies what stages this graph builder provides (i.e. streets,
     * elevation, transit)
     */
    public List<String> provides();
    
    /** A list of ids of stages which must be provided before this stage */
    public List<String> getPrerequisites();

    /** Check that all inputs to the graphbuilder are valid; throw an exception if not */
    public void checkInputs();
}
