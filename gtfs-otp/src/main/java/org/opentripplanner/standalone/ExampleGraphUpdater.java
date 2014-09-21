package org.opentripplanner.standalone;

import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class shows an example of how to implement a graph updater. Besides implementing the methods
 * of the interface GraphUpdater, the updater also needs to be registered in the function
 * GraphUpdaterConfigurator.applyConfigurationToGraph. This example is suited for streaming
 * updaters. For polling updaters it is better to use the abstract base class PollingGraphUpdater.
 * The class ExamplePollingGraphUpdater shows an example of this. Usage example ('example' name is
 * an example) in the file 'Graph.properties':
 *
 * <pre>
 * example.type = example-updater
 * example.frequencySec = 60
 * example.url = https://api.updater.com/example-updater
 * </pre>
 *
 * @see ExamplePollingGraphUpdater
 * @see GraphUpdaterConfigurator.applyConfigurationToGraph
 */
public class ExampleGraphUpdater implements GraphUpdater {
    
    private static Logger LOG = LoggerFactory.getLogger(ExampleGraphUpdater.class);
    
    private GraphUpdaterManager updaterManager;
    
    private Integer frequencySec;
    
    private String url;
    
    // Here the updater can be configured using the properties in the file 'Graph.properties'.
    @Override
    public void configure(Graph graph, Preferences preferences) throws Exception {
        this.frequencySec = preferences.getInt("frequencySec", 5);
        this.url = preferences.get("url", null);
        LOG.info("Configured example updater: frequencySec={} and url={}", this.frequencySec, this.url);
    }
    
    // Here the updater gets to know its parent manager to execute GraphWriterRunnables.
    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        LOG.info("Example updater: updater manager is set");
        this.updaterManager = updaterManager;
    }
    
    // Here the updater can be initialized.
    @Override
    public void setup() {
        LOG.info("Setup example updater");

        // Execute anonymous graph writer runnable and wait for its termination
        try {
            this.updaterManager.executeBlocking(new GraphWriterRunnable() {
                @Override
                public void run(Graph graph) {
                    LOG.info("Anonymous graph writer {} runnable is run on the " + "graph writer scheduler.", this.hashCode());
                    // Do some writing to the graph here
                }
            });
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }
    
    // This is where the updater thread receives updates and applies them to the graph.
    // This method only runs once.
    @Override
    public void run() {
        LOG.info("Run example updater with hashcode: {}", this.hashCode());
        // Here the updater can connect to a server and register a callback function
        // to handle updates to the graph
    }
    
    // Here the updater can cleanup after itself.
    @Override
    public void teardown() {
        LOG.info("Teardown example updater");
    }
    
}
