package com.trein.gtfs.otp;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.annotation.PreDestroy;

import org.opentripplanner.standalone.Graph.LoadLevel;
import org.opentripplanner.standalone.StreetVertexIndexFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The primary implementation of the GraphService interface. It can handle multiple graphs, each
 * with its own routerId. These graphs are loaded from serialized graph files in subdirectories
 * immediately under the specified base resource/filesystem path. Delegate the file loading
 * implementation details to the GraphServiceFileImpl.
 *
 * @see GraphServiceFileImpl
 */
public class GraphServiceImpl implements GraphService {
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphServiceImpl.class);
    
    private final GraphServiceFileImpl decorated = new GraphServiceFileImpl();
    
    /** A list of routerIds to automatically register and load at startup */
    public List<String> autoRegister;
    
    /** If true, on startup register the graph in the location defaultRouterId. */
    private final boolean attemptRegisterDefault = true;
    
    /**
     * @param indexFactory
     */
    public void setIndexFactory(StreetVertexIndexFactory indexFactory) {
        this.decorated.indexFactory = (indexFactory);
    }
    
    /**
     * @param defaultRouterId
     */
    public void setDefaultRouterId(String defaultRouterId) {
        this.decorated.defaultRouterId = (defaultRouterId);
    }
    
    /**
     * Sets a base path for graph loading from the filesystem. Serialized graph files will be
     * retrieved from sub-directories immediately below this directory. The routerId of a graph is
     * the same as the name of its sub-directory. This does the same thing as setResource, except
     * the parameter is interpreted as a file path.
     */
    public void setPath(String path) {
        this.decorated.basePath = (path);
    }
    
    /**
     * Based on the autoRegister list, automatically register all routerIds for which we can find a
     * graph file in a subdirectory of the resourceBase path. Also register and load the graph for
     * the defaultRouterId and warn if no routerIds are registered.
     */
    public void startup() {
        if ((this.autoRegister != null) && !this.autoRegister.isEmpty()) {
            LOG.info("attempting to automatically register routerIds {}", this.autoRegister);
            LOG.info("graph files will be sought in paths relative to {}", this.decorated.basePath);
            for (String routerId : this.autoRegister) {
                registerGraph(routerId, true);
            }
        } else {
            LOG.info("no list of routerIds was provided for automatic registration.");
        }
        if (this.attemptRegisterDefault && !this.decorated.getRouterIds().contains(this.decorated.defaultRouterId)) {
            LOG.info("Attempting to load graph for default routerId '{}'.", this.decorated.defaultRouterId);
            registerGraph(this.decorated.defaultRouterId, true);
        }
        if (this.getRouterIds().isEmpty()) {
            LOG.warn("No graphs have been loaded/registered. "
                    + "You must use the routers API to register one or more graphs before routing.");
        }
    }

    /**
     * This is called when the bean gets deleted, that is mainly in case of webapp container
     * application stop or reload. We teardown all loaded graph to stop their background real-time
     * data updater thread.
     */
    @PreDestroy
    private void teardown() {
        LOG.info("Cleaning-up graphs...");
        evictAll();
        this.decorated.cleanupWebapp();
    }
    
    @Override
    public Graph getGraph() {
        return this.decorated.getGraph();
    }
    
    @Override
    public Graph getGraph(String routerId) {
        return this.decorated.getGraph(routerId);
    }
    
    @Override
    public void setLoadLevel(LoadLevel level) {
        this.decorated.setLoadLevel(level);
    }
    
    @Override
    public boolean reloadGraphs(boolean preEvict) {
        return this.decorated.reloadGraphs(preEvict);
    }
    
    @Override
    public Collection<String> getRouterIds() {
        return this.decorated.getRouterIds();
    }
    
    @Override
    public boolean registerGraph(String routerId, boolean preEvict) {
        return this.decorated.registerGraph(routerId, preEvict);
    }
    
    @Override
    public boolean registerGraph(String routerId, Graph graph) {
        return this.decorated.registerGraph(routerId, graph);
    }
    
    @Override
    public boolean evictGraph(String routerId) {
        return this.decorated.evictGraph(routerId);
    }
    
    @Override
    public int evictAll() {
        return this.decorated.evictAll();
    }
    
    @Override
    public boolean save(String routerId, InputStream is) {
        return this.decorated.save(routerId, is);
    }
}
