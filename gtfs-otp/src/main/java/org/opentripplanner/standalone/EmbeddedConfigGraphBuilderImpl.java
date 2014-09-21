package org.opentripplanner.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embed inside the graph a default configuration used when decorating the graph during load.
 */
public class EmbeddedConfigGraphBuilderImpl implements GraphBuilder {
    
    public File propertiesFile;
    
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedConfigGraphBuilderImpl.class);
    
    public void setPropertiesPath(String propertiesPath) {
        this.propertiesFile = new File(propertiesPath);
    }
    
    /**
     * An set of ids which identifies what stages this graph builder provides (i.e. streets,
     * elevation, transit)
     */
    @Override
    public List<String> provides() {
        return Collections.emptyList();
    }
    
    /** A list of ids of stages which must be provided before this stage */
    @Override
    public List<String> getPrerequisites() {
        return Collections.emptyList();
    }
    
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        try {
            LOG.info("Bundling config '" + this.propertiesFile.getPath() + "' into graph.");
            Properties props = new Properties();
            props.load(new FileInputStream(this.propertiesFile));
            graph.embeddedPreferences = props;
        } catch (IOException e) {
            LOG.error("Can't load properties from '" + this.propertiesFile.getAbsolutePath() + "'", e);
        }
    }
    
    @Override
    public void checkInputs() {
        if (!this.propertiesFile.canRead()) { throw new IllegalArgumentException("Configuration '"
                + this.propertiesFile.getAbsolutePath() + "' can't be read."); }
    }
}
