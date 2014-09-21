package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Lists;

/**
 * Find the shortest path between graph vertices using A*.
 */
public class GenericAStar implements SPTService { // maybe this should be wrapped in a component SPT
                                                  // service

    private static final Logger LOG = LoggerFactory.getLogger(GenericAStar.class);
    private static final MonitoringStore store = MonitoringStoreFactory.getStore();
    private static final double OVERSEARCH_MULTIPLIER = 4.0;
    
    private final boolean verbose = false;
    
    private TraverseVisitor traverseVisitor;
    
    enum RunStatus {
        RUNNING, STOPPED
    }
    
    class RunState {
        
        public State u;
        public ShortestPathTree spt;
        BinHeap<State> pq;
        RemainingWeightHeuristic heuristic;
        public RoutingContext rctx;
        public int nVisited;
        public List<Object> targetAcceptedStates;
        public RunStatus status;
        private final RoutingRequest options;
        private final SearchTerminationStrategy terminationStrategy;
        public Vertex u_vertex;
        Double foundPathWeight = null;
        
        public RunState(RoutingRequest options, SearchTerminationStrategy terminationStrategy) {
            this.options = options;
            this.terminationStrategy = terminationStrategy;
        }
        
    }

    private RunState runState;

    /**
     * Compute SPT using default timeout and termination strategy.
     */
    @Override
    public ShortestPathTree getShortestPathTree(RoutingRequest req) {
        return getShortestPathTree(req, -1, null); // negative timeout means no timeout
    }

    /**
     * Compute SPT using default termination strategy.
     */
    @Override
    public ShortestPathTree getShortestPathTree(RoutingRequest req, double timeoutSeconds) {
        return this.getShortestPathTree(req, timeoutSeconds, null);
    }

    public void startSearch(RoutingRequest options, SearchTerminationStrategy terminationStrategy, long abortTime) {
        this.runState = new RunState(options, terminationStrategy);
        
        this.runState.rctx = options.getRoutingContext();
        
        // null checks on origin and destination vertices are already performed in setRoutingContext
        // options.rctx.check();

        this.runState.spt = new MultiShortestPathTree(this.runState.options);
        
        this.runState.heuristic = options.batch ? new TrivialRemainingWeightHeuristic()
                : this.runState.rctx.remainingWeightHeuristic;
        
        // heuristic calc could actually be done when states are constructed, inside state
                State initialState = new State(options);
                this.runState.heuristic.initialize(initialState, this.runState.rctx.target, abortTime);
                if ((abortTime < Long.MAX_VALUE) && (System.currentTimeMillis() > abortTime)) {
                    LOG.warn("Timeout during initialization of interleaved bidirectional heuristic.");
                    options.rctx.debugOutput.timedOut = true;
                    this.runState = null; // Search timed out
                    return;
                }
                this.runState.spt.add(initialState);
        
        // Priority Queue.
                // NOTE(flamholz): the queue is self-resizing, so we initialize it to have
        // size = O(sqrt(|V|)) << |V|. For reference, a random, undirected search
                // on a uniform 2d grid will examine roughly sqrt(|V|) vertices before
                // reaching its target.
        int initialSize = this.runState.rctx.graph.getVertices().size();
                initialSize = (int) Math.ceil(2 * (Math.sqrt((double) initialSize + 1)));
                this.runState.pq = new BinHeap<State>(initialSize);
                this.runState.pq.insert(initialState, 0);
        
        // options = options.clone();
        // /** max walk distance cannot be less than distances to nearest transit stops */
        // double minWalkDistance = origin.getVertex().getDistanceToNearestTransitStop()
        // + target.getDistanceToNearestTransitStop();
        // options.setMaxWalkDistance(Math.max(options.getMaxWalkDistance(),
        // rctx.getMinWalkDistance()));
        
        this.runState.nVisited = 0;
                this.runState.targetAcceptedStates = Lists.newArrayList();
        
    }
    
    boolean iterate() {
        // print debug info
        if (this.verbose) {
            double w = this.runState.pq.peek_min_key();
            System.out.println("pq min key = " + w);
        }

        // interleave some heuristic-improving work (single threaded)
        this.runState.heuristic.doSomeWork();
        
        // get the lowest-weight state in the queue
        this.runState.u = this.runState.pq.extract_min();

        // check that this state has not been dominated
        // and mark vertex as visited
        if (!this.runState.spt.visit(this.runState.u)) {
            // state has been dominated since it was added to the priority queue, so it is
            // not in any optimal path. drop it on the floor and try the next one.
            return false;
        }

        if (this.traverseVisitor != null) {
            this.traverseVisitor.visitVertex(this.runState.u);
        }

        this.runState.u_vertex = this.runState.u.getVertex();
        
        if (this.verbose) {
            System.out.println("   vertex " + this.runState.u_vertex);
        }
        
        this.runState.nVisited += 1;

        Collection<Edge> edges = this.runState.options.arriveBy ? this.runState.u_vertex.getIncoming() : this.runState.u_vertex
                .getOutgoing();
        for (Edge edge : edges) {
            
            // Iterate over traversal results. When an edge leads nowhere (as indicated by
            // returning NULL), the iteration is over. TODO Use this to board multiple trips.
            for (State v = edge.traverse(this.runState.u); v != null; v = v.getNextResult()) {
                // Could be: for (State v : traverseEdge...)
                
                if (this.traverseVisitor != null) {
                    this.traverseVisitor.visitEdge(edge, v);
                }
                // TEST: uncomment to verify that all optimisticTraverse functions are actually
                // admissible
                // State lbs = edge.optimisticTraverse(u);
                // if ( ! (lbs.getWeight() <= v.getWeight())) {
                // System.out.printf("inadmissible lower bound %f vs %f on edge %s\n",
                // lbs.getWeightDelta(), v.getWeightDelta(), edge);
                // }
                
                double remaining_w = computeRemainingWeight(this.runState.heuristic, v, this.runState.rctx.target,
                        this.runState.options);
                
                if ((remaining_w < 0) || Double.isInfinite(remaining_w)) {
                    continue;
                }
                double estimate = v.getWeight() + (remaining_w * this.runState.options.heuristicWeight);
                
                if (this.verbose) {
                    System.out.println("      edge " + edge);
                    System.out.println("      " + this.runState.u.getWeight() + " -> " + v.getWeight() + "(w) + " + remaining_w
                            + "(heur) = " + estimate + " vert = " + v.getVertex());
                }
                
                // avoid enqueuing useless branches
                if (estimate > this.runState.options.maxWeight) {
                    // too expensive to get here
                    if (this.verbose) {
                        System.out.println("         too expensive to reach, not enqueued. estimated weight = " + estimate);
                    }
                    continue;
                }
                if (isWorstTimeExceeded(v, this.runState.options)) {
                    // too much time to get here
                    if (this.verbose) {
                        System.out.println("         too much time to reach, not enqueued. time = " + v.getTimeSeconds());
                    }
                    continue;
                }

                // spt.add returns true if the state is hopeful; enqueue state if it's hopeful
                if (this.runState.spt.add(v)) {
                    // report to the visitor if there is one
                    if (this.traverseVisitor != null) {
                        this.traverseVisitor.visitEnqueue(v);
                    }

                    this.runState.pq.insert(v, estimate);
                }
            }
        }

        return true;
    }

    void runSearch(long abortTime) {
        /* the core of the A* algorithm */
        while (!this.runState.pq.empty()) { // Until the priority queue is empty:
            /*
             * Terminate based on timeout?
             */
            if ((abortTime < Long.MAX_VALUE) && (System.currentTimeMillis() > abortTime)) {
                LOG.warn("Search timeout. origin={} target={}", this.runState.rctx.origin, this.runState.rctx.target);
                // Rather than returning null to indicate that the search was aborted/timed out,
                // we instead set a flag in the routing context and return the SPT anyway. This
                // allows returning a partial list results even when a timeout occurs.
                this.runState.options.rctx.aborted = true; // signal search cancellation up to
                                                           // higher stack frames
                this.runState.options.rctx.debugOutput.timedOut = true; // signal timeout in debug
                                                                        // output object
                
                break;
            }

            /*
             * Get next best state and, if it hasn't already been dominated, add adjacent states to
             * queue. If it has been dominated, the iteration is over; don't bother checking for
             * termination condition. Note that termination is checked after adjacent states are
             * added. This presents the small inefficiency that adjacent states are generated for a
             * state which could be the last one you need to check. The advantage of this is that
             * the algorithm is always left in a restartable state, which is useful for debugging or
             * potential future variations.
             */
            if (!iterate()) {
                continue;
            }

            /*
             * Should we terminate the search?
             */
            // Don't search too far past the most recently found accepted path/state
            if ((this.runState.foundPathWeight != null)
                    && (this.runState.u.getWeight() > (this.runState.foundPathWeight * OVERSEARCH_MULTIPLIER))) {
                
                break;
            }
            if (this.runState.terminationStrategy != null) {
                if (!this.runState.terminationStrategy.shouldSearchContinue(this.runState.rctx.origin, this.runState.rctx.target,
                        this.runState.u, this.runState.spt, this.runState.options)) {
                    break;
                    // TODO AMB: Replace isFinal with bicycle conditions in BasicPathParser
                }
            } else if (!this.runState.options.batch && (this.runState.u_vertex == this.runState.rctx.target)
                    && this.runState.u.isFinal() && this.runState.u.allPathParsersAccept()) {
                this.runState.targetAcceptedStates.add(this.runState.u);
                this.runState.foundPathWeight = this.runState.u.getWeight();
                this.runState.options.rctx.debugOutput.foundPath();
                if (this.runState.targetAcceptedStates.size() >= this.runState.options.getNumItineraries()) {
                    LOG.debug("total vertices visited {}", this.runState.nVisited);
                    
                    break;
                }
            }
            
        }
    }
    
    /** @return the shortest path, or null if none is found */
    @Override
    public ShortestPathTree getShortestPathTree(RoutingRequest options, double relTimeout,
            SearchTerminationStrategy terminationStrategy) {
        ShortestPathTree spt = null;
        long abortTime = DateUtils.absoluteTimeout(relTimeout);
        
        startSearch(options, terminationStrategy, abortTime);
        
        if (this.runState != null) {
            runSearch(abortTime);
            spt = this.runState.spt;
        }

        storeMemory();
        return spt;
    }
    
    private void storeMemory() {
        if (store.isMonitoring("memoryUsed")) {
            System.gc();
            long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            store.setLongMax("memoryUsed", memoryUsed);
        }
    }
    
    private double computeRemainingWeight(final RemainingWeightHeuristic heuristic, State v, Vertex target, RoutingRequest options) {
        // actually, the heuristic could figure this out from the TraverseOptions.
        // set private member back=options.isArriveBy() on initial weight computation.
        if (options.arriveBy) {
            return heuristic.computeReverseWeight(v, target);
        } else {
            return heuristic.computeForwardWeight(v, target);
        }
    }
    
    private boolean isWorstTimeExceeded(State v, RoutingRequest opt) {
        if (opt.arriveBy) {
            return v.getTimeSeconds() < opt.worstTime;
        } else {
            return v.getTimeSeconds() > opt.worstTime;
        }
    }
    
    public void setTraverseVisitor(TraverseVisitor traverseVisitor) {
        this.traverseVisitor = traverseVisitor;
    }
}
