package org.opentripplanner.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opentripplanner.standalone.Graph.LoadLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphBuilderTask implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(GraphBuilderTask.class);
    
    private List<GraphBuilder> _graphBuilders = new ArrayList<GraphBuilder>();
    
    private File graphFile;

    private boolean _alwaysRebuild = true;
    
    private List<RoutingRequest> _modeList;

    private String _baseGraph = null;

    private Graph graph = new Graph();
    
    /** Should the graph be serialized to disk after being created or not? */
    public boolean serializeGraph = true;
    
    public void addGraphBuilder(GraphBuilder loader) {
        this._graphBuilders.add(loader);
    }
    
    public void setGraphBuilders(List<GraphBuilder> graphLoaders) {
        this._graphBuilders = graphLoaders;
    }
    
    public void setAlwaysRebuild(boolean alwaysRebuild) {
        this._alwaysRebuild = alwaysRebuild;
    }

    public void setBaseGraph(String baseGraph) {
        this._baseGraph = baseGraph;
        try {
            this.graph = Graph.load(new File(baseGraph), LoadLevel.FULL);
        } catch (Exception e) {
            throw new RuntimeException("error loading base graph");
        }
    }
    
    public void addMode(RoutingRequest mo) {
        this._modeList.add(mo);
    }
    
    public void setModes(List<RoutingRequest> modeList) {
        this._modeList = modeList;
    }

    public void setPath(String path) {
        this.graphFile = new File(path.concat("/Graph.obj"));
    }

    public void setPath(File path) {
        this.graphFile = new File(path, "Graph.obj");
    }
    
    public Graph getGraph() {
        return this.graph;
    }
    
    @Override
    public void run() {

        if (this.graphFile == null) { throw new RuntimeException("graphBuilderTask has no attribute graphFile."); }
        
        if (this.graphFile.exists() && !this._alwaysRebuild) {
            LOG.info("graph already exists and alwaysRebuild=false => skipping graph build");
            return;
        }
        
        if (this.serializeGraph) {
            try {
                if (!this.graphFile.getParentFile().exists()) {
                    if (!this.graphFile.getParentFile().mkdirs()) {
                        LOG.error("Failed to create directories for graph bundle at " + this.graphFile);
                    }
                }
                this.graphFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Cannot create or overwrite graph at path " + this.graphFile);
            }
        }
        
        // check prerequisites
        ArrayList<String> provided = new ArrayList<String>();
        boolean bad = false;
        for (GraphBuilder builder : this._graphBuilders) {
            List<String> prerequisites = builder.getPrerequisites();
            for (String prereq : prerequisites) {
                if (!provided.contains(prereq)) {
                    LOG.error("Graph builder " + builder + " requires " + prereq + " but no previous stages provide it");
                    bad = true;
                }
            }
            provided.addAll(builder.provides());
        }
        if (this._baseGraph != null) {
            LOG.warn("base graph loaded, not enforcing prerequisites");
        } else if (bad) { throw new RuntimeException("Prerequisites unsatisfied"); }
        
        // check inputs
        for (GraphBuilder builder : this._graphBuilders) {
            builder.checkInputs();
        }

        HashMap<Class<?>, Object> extra = new HashMap<Class<?>, Object>();
        for (GraphBuilder load : this._graphBuilders) {
            load.buildGraph(this.graph, extra);
        }
        
        this.graph.summarizeBuilderAnnotations();
        if (this.serializeGraph) {
            try {
                this.graph.save(this.graphFile);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            LOG.info("Not saving graph to disk, as requested.");
            this.graph.index(new DefaultStreetVertexIndexFactory());
        }

    }
}
