package org.opentripplanner.standalone;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.opentripplanner.standalone.Graph.LoadLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the file-based GraphServiceFileImpl which auto-configure itself by scanning
 * the root resource directory.
 */
public class GraphServiceAutoDiscoverImpl implements GraphService {
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphServiceAutoDiscoverImpl.class);
    
    private final GraphServiceFileImpl decorated = new GraphServiceFileImpl();
    
    /** Last timestamp upper bound when we auto-scanned resources. */
    private long lastAutoScan = 0L;
    
    /** The autoscan period in seconds */
    private final int autoScanPeriodSec = 60;
    
    private final ScheduledExecutorService scanExecutor = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * The delay before loading a new graph, in seconds. We load a graph if it has been modified at
     * least this amount of time in the past. This in order to give some time for non-atomic graph
     * copy.
     */
    private final int loadDelaySec = 10;
    
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
        // Invalid in auto-discovery mode
        return false;
    }
    
    @Override
    public boolean registerGraph(String routerId, Graph graph) {
        // Invalid in auto-discovery mode
        return false;
    }
    
    @Override
    public boolean evictGraph(String routerId) {
        // Invalid in auto-discovery mode
        return false;
    }
    
    @Override
    public int evictAll() {
        // Invalid in auto-discovery mode
        return 0;
    }
    
    @Override
    public boolean save(String routerId, InputStream is) {
        return this.decorated.save(routerId, is);
    }
    
    /**
     * Based on the autoRegister list, automatically register all routerIds for which we can find a
     * graph file in a subdirectory of the resourceBase path. Also register and load the graph for
     * the defaultRouterId and warn if no routerIds are registered.
     */
    public void startup() {
        /* Run the first one syncronously as other initialization methods may need a default router. */
        autoDiscoverGraphs();
        /*
         * Starting with JDK7 we should use a directory change listener callback on baseResource
         * instead.
         */
        this.scanExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                autoDiscoverGraphs();
            }
        }, this.autoScanPeriodSec, this.autoScanPeriodSec, TimeUnit.SECONDS);
    }
    
    /**
     * This is called when the bean gets deleted, that is mainly in case of webapp container
     * application stop or reload. We teardown all loaded graph to stop their background real-time
     * data updater thread, and also the background auto-discover scanner thread.
     */
    @PreDestroy
    private void teardown() {
        LOG.info("Cleaning-up auto-discover thread and graphs");
        this.decorated.evictAll();
        this.scanExecutor.shutdown();
        try {
            boolean noTimeout = this.scanExecutor.awaitTermination(10, TimeUnit.SECONDS);
            if (!noTimeout) {
                LOG.warn("Timeout while waiting for scanner thread to finish");
            }
        } catch (InterruptedException e) {
            // This is not really important
            LOG.warn("Interrupted while waiting for scanner thread to finish", e);
        }
        this.decorated.cleanupWebapp();
    }
    
    private synchronized void autoDiscoverGraphs() {
        LOG.debug("Auto discovering graphs under {}", this.decorated.basePath);
        Collection<String> graphOnDisk = new HashSet<String>();
        Collection<String> graphToLoad = new HashSet<String>();
        // Only reload graph modified more than 1 mn ago.
        long validEndTime = System.currentTimeMillis() - (this.loadDelaySec * 1000);
        File baseFile = new File(this.decorated.basePath);
        // First check for a root graph
        File rootGraphFile = new File(baseFile, GraphServiceFileImpl.GRAPH_FILENAME);
        if (rootGraphFile.exists() && rootGraphFile.canRead()) {
            graphOnDisk.add("");
            // lastModified can change, so test must be atomic here.
            long lastModified = rootGraphFile.lastModified();
            if ((lastModified > this.lastAutoScan) && (lastModified <= validEndTime)) {
                LOG.debug("Graph to (re)load: {}, lastModified={}", rootGraphFile, lastModified);
                graphToLoad.add("");
            }
        }
        // Then graph in sub-directories
        for (String sub : baseFile.list()) {
            File subFile = new File(baseFile, sub);
            if (subFile.isDirectory()) {
                File graphFile = new File(subFile, GraphServiceFileImpl.GRAPH_FILENAME);
                if (graphFile.exists() && graphFile.canRead()) {
                    graphOnDisk.add(sub);
                    long lastModified = graphFile.lastModified();
                    if ((lastModified > this.lastAutoScan) && (lastModified <= validEndTime)) {
                        LOG.debug("Graph to (re)load: {}, lastModified={}", graphFile, lastModified);
                        graphToLoad.add(sub);
                    }
                }
            }
        }
        this.lastAutoScan = validEndTime;
        
        StringBuffer onDiskSb = new StringBuffer();
        for (String routerId : graphOnDisk) {
            onDiskSb.append("[").append(routerId).append("]");
        }
        StringBuffer toLoadSb = new StringBuffer();
        for (String routerId : graphToLoad) {
            toLoadSb.append("[").append(routerId).append("]");
        }
        LOG.debug("Found routers: {} - Must reload: {}", onDiskSb.toString(), toLoadSb.toString());
        for (String routerId : graphToLoad) {
            /*
             * Do not set preEvict, because: 1) during loading of a new graph we want to keep one
             * available; and 2) if the loading of a new graph fails we also want to keep the old
             * one.
             */
            this.decorated.registerGraph(routerId, false);
        }
        for (String routerId : getRouterIds()) {
            // Evict graph removed from disk.
            if (!graphOnDisk.contains(routerId)) {
                LOG.warn("Auto-evicting routerId '{}', not present on disk anymore.", routerId);
                this.decorated.evictGraph(routerId);
            }
        }
        
        /*
         * If the defaultRouterId is not present, print a warning and set it to some default.
         */
        if (!getRouterIds().contains(this.decorated.defaultRouterId)) {
            LOG.warn("Default routerId '{}' not available!", this.decorated.defaultRouterId);
            if (!getRouterIds().isEmpty()) {
                // Let's see which one we want to take by default
                String defRouterId = null;
                if (getRouterIds().contains("")) {
                    // If we have a root graph, this should be a good default
                    defRouterId = "";
                    LOG.info("Setting default routerId to root graph ''");
                } else {
                    // Otherwise take first one present
                    defRouterId = getRouterIds().iterator().next();
                    if (getRouterIds().size() > 1) {
                        LOG.warn("Setting default routerId to arbitrary one '{}'", defRouterId);
                    } else {
                        LOG.info("Setting default routerId to '{}'", defRouterId);
                    }
                }
                this.decorated.defaultRouterId = (defRouterId);
            }
        }
        if (this.getRouterIds().isEmpty()) {
            LOG.warn("No graphs have been loaded/registered. " + "You must place one or more graphs before routing.");
        }
    }
    
}
