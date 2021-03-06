package com.trein.gtfs.otp.building.graph.osm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * this module is part of the {@link GraphBuilder} process. it design to remove small isolated
 * islands form the graph. Islands are created when there is no connectivity in the map, island acts
 * like trap since there is no connectivity there is no way in or out the island. The module
 * distinguish between two island types one with transit stops and one without stops.
 */
public class PruneFloatingIslands implements GraphBuilder {
    
    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(PruneFloatingIslands.class);
    
    /**
     * this field indicate the maximum size for island without stops island under this size will be
     * pruned.
     */
    private final int islandWithoutStopsMaxSize = 40;
    
    /**
     * this field indicate the maximum size for island with stops island under this size will be
     * pruned.
     */
    private final int islandWithStopsMaxSize = 5;
    
    /**
     * The name for output file for this process. The file will store information about the islands
     * that were found and whether they were pruned. If the value is an empty string or null there
     * will be no output file.
     */
    private String islandLogFile;
    
    private TransitToStreetNetworkGraphBuilderImpl transitToStreetNetwork;
    
    @Override
    public List<String> provides() {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getPrerequisites() {
        /**
         * this module can run after the street module only but if the street linker did not run
         * then it couldn't identifies island with stops. so if the need is to distinguish between
         * island with stops or without stops as explained before this module should run after the
         * streets and the linker modules.
         */
        return Arrays.asList("streets");
    }
    
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        LOG.info("Pruning isolated islands in street network");

        StreetUtils.pruneFloatingIslands(graph, this.islandWithoutStopsMaxSize, this.islandWithStopsMaxSize, this.islandLogFile);
        if (this.transitToStreetNetwork == null) {
            LOG.debug("TransitToStreetNetworkGraphBuilder was not provided to PruneFloatingIslands. Not attempting to reconnect stops.");
        } else {
            // reconnect stops on small islands (that removed)
            this.transitToStreetNetwork.buildGraph(graph, extra);
        }
        LOG.debug("Done pruning isolated islands");
    }
    
    @Override
    public void checkInputs() {
        // no inputs
    }
    
}
