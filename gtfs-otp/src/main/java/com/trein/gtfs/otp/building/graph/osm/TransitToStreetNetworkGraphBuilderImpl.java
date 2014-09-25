package com.trein.gtfs.otp.building.graph.osm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GraphBuilder} plugin that links up the stops of a transit network to a street network.
 * Should be called after both the transit network and street network are loaded.
 */
public class TransitToStreetNetworkGraphBuilderImpl implements GraphBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransitToStreetNetworkGraphBuilderImpl.class);
    
    @Override
    public List<String> provides() {
        return Arrays.asList("street to transit", "linking");
    }
    
    @Override
    public List<String> getPrerequisites() {
        return Arrays.asList("streets"); // why not "transit" ?
    }
    
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        LOG.info("Linking transit stops to streets...");
        NetworkLinker linker = new NetworkLinker(graph, extra);
        linker.createLinkage();
    }
    
    @Override
    public void checkInputs() {
        // no inputs
    }
}
