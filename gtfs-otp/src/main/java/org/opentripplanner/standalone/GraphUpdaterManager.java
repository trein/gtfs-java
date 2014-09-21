package org.opentripplanner.standalone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * This class is attached to the graph:
 *
 * <pre>
 * GraphUpdaterManager updaterManager = graph.getUpdaterManager();
 * </pre>
 *
 * Each updater will run in its own thread. When changes to the graph have to be made by these
 * updaters, this should be done via the execute method of this manager to prevent race conditions
 * between graph write operations.
 */
public class GraphUpdaterManager {
    
    private static Logger LOG = LoggerFactory.getLogger(GraphUpdaterManager.class);

    /**
     * Text used for naming threads when the graph lacks a routerId.
     */
    private static String DEFAULT_ROUTER_ID = "(default)";

    /**
     * Thread factory used to create new threads.
     */

    private final ThreadFactory threadFactory;
    
    /**
     * OTP's multi-version concurrency control model for graph updating allows simultaneous reads,
     * but never simultaneous writes. We ensure this policy is respected by having a single writer
     * thread, which sequentially executes all graph updater tasks. Each task is a runnable that is
     * scheduled with the ExecutorService to run at regular intervals.
     */
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * Pool with updaters
     */
    private ExecutorService updaterPool = Executors.newCachedThreadPool();
    
    /**
     * List with updaters to be able to free resources TODO: is this list necessary?
     */
    List<GraphUpdater> updaterList = new ArrayList<GraphUpdater>();
    
    /**
     * Parent graph of this manager
     */
    Graph graph;
    
    /**
     * Constructor
     *
     * @param graph is parent graph of manager
     */
    public GraphUpdaterManager(Graph graph) {
        this.graph = graph;

        String routerId = graph.routerId;
        if ((routerId == null) || routerId.isEmpty()) {
            routerId = DEFAULT_ROUTER_ID;
        }

        this.threadFactory = new ThreadFactoryBuilder().setNameFormat("GraphUpdater-" + routerId + "-%d").build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
        this.updaterPool = Executors.newCachedThreadPool(this.threadFactory);
    }
    
    public void stop() {
        // TODO: find a better way to stop these threads
        
        // Shutdown updaters
        this.updaterPool.shutdownNow();
        try {
            boolean ok = this.updaterPool.awaitTermination(30, TimeUnit.SECONDS);
            if (!ok) {
                LOG.warn("Timeout waiting for updaters to finish.");
            }
        } catch (InterruptedException e) {
            // This should not happen
            LOG.warn("Interrupted while waiting for updaters to finish.");
        }
        
        // Clean up updaters
        for (GraphUpdater updater : this.updaterList) {
            updater.teardown();
        }
        this.updaterList.clear();
        
        // Shutdown scheduler
        this.scheduler.shutdownNow();
        try {
            boolean ok = this.scheduler.awaitTermination(30, TimeUnit.SECONDS);
            if (!ok) {
                LOG.warn("Timeout waiting for scheduled task to finish.");
            }
        } catch (InterruptedException e) {
            // This should not happen
            LOG.warn("Interrupted while waiting for scheduled task to finish.");
        }
    }
    
    /**
     * Adds an updater to the manager and runs it immediately in its own thread.
     *
     * @param updater is the updater to add and run
     */
    public void addUpdater(final GraphUpdater updater) {
        this.updaterList.add(updater);
        this.updaterPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    updater.setup();
                    try {
                        updater.run();
                    } catch (Exception e) {
                        LOG.error("Error while running updater {}:", updater.getClass().getName(), e);
                    }
                } catch (Exception e) {
                    LOG.error("Error while setting up updater {}:", updater.getClass().getName(), e);
                }
            }
        });
    }
    
    /**
     * This is the method to use to modify the graph from the updaters. The runnables will be
     * scheduled after each other, guaranteeing that only one of these runnables will be active at
     * any time.
     *
     * @param runnable is a graph writer runnable
     */
    public void execute(GraphWriterRunnable runnable) {
        executeReturningFuture(runnable);
    }
    
    /**
     * This is another method to use to modify the graph from the updaters. It behaves like execute,
     * but blocks until the runnable has been executed. This might be particularly useful in the
     * setup method of an updater.
     *
     * @param runnable is a graph writer runnable
     * @throws ExecutionException
     * @throws InterruptedException
     * @see GraphUpdaterManager.execute
     */
    public void executeBlocking(GraphWriterRunnable runnable) throws InterruptedException, ExecutionException {
        Future<?> future = executeReturningFuture(runnable);
        // Ask for result of future. Will block and return null when runnable is successfully
        // finished, throws otherwise
        future.get();
    }
    
    private Future<?> executeReturningFuture(final GraphWriterRunnable runnable) {
        // TODO: check for high water mark?
        Future<?> future = this.scheduler.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run(GraphUpdaterManager.this.graph);
                } catch (Exception e) {
                    LOG.error("Error while running graph writer {}:", runnable.getClass().getName(), e);
                }
            }
        });
        return future;
    }
    
    public int size() {
        return this.updaterList.size();
    }
    
}
