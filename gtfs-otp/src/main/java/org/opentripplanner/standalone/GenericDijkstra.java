package org.opentripplanner.standalone;

/**
 * Find the shortest path between graph vertices using Dijkstra's algorithm.
 */
public class GenericDijkstra {
    
    private final RoutingRequest options;
    
    private SearchTerminationStrategy searchTerminationStrategy;
    
    private SkipEdgeStrategy skipEdgeStrategy;
    
    private SkipTraverseResultStrategy skipTraverseResultStrategy;
    
    private final boolean verbose = false;
    
    private RemainingWeightHeuristic heuristic = new TrivialRemainingWeightHeuristic();
    
    public GenericDijkstra(RoutingRequest options) {
        this.options = options;
    }
    
    public void setSearchTerminationStrategy(SearchTerminationStrategy searchTerminationStrategy) {
        this.searchTerminationStrategy = searchTerminationStrategy;
    }
    
    public void setSkipEdgeStrategy(SkipEdgeStrategy skipEdgeStrategy) {
        this.skipEdgeStrategy = skipEdgeStrategy;
    }
    
    public void setSkipTraverseResultStrategy(SkipTraverseResultStrategy skipTraverseResultStrategy) {
        this.skipTraverseResultStrategy = skipTraverseResultStrategy;
    }
    
    public ShortestPathTree getShortestPathTree(State initialState) {
        Vertex target = null;
        if (this.options.rctx != null) {
            target = initialState.getOptions().rctx.target;
        }
        ShortestPathTree spt = new BasicShortestPathTree(this.options);
        BinHeap<State> queue = new BinHeap<State>(1000);
        
        spt.add(initialState);
        queue.insert(initialState, initialState.getWeight());
        
        while (!queue.empty()) { // Until the priority queue is empty:
            State u = queue.extract_min();
            Vertex u_vertex = u.getVertex();
            
            if (!spt.getStates(u_vertex).contains(u)) {
                continue;
            }
            
            if (this.verbose) {
                System.out.println("min," + u.getWeight());
                System.out.println(u_vertex);
            }
            
            if ((this.searchTerminationStrategy != null)
                    && !this.searchTerminationStrategy.shouldSearchContinue(initialState.getVertex(), null, u, spt, this.options)) {
                break;
            }
            
            for (Edge edge : this.options.arriveBy ? u_vertex.getIncoming() : u_vertex.getOutgoing()) {
                
                if ((this.skipEdgeStrategy != null)
                        && this.skipEdgeStrategy.shouldSkipEdge(initialState.getVertex(), null, u, edge, spt, this.options)) {
                    continue;
                }
                
                // Iterate over traversal results. When an edge leads nowhere (as indicated by
                // returning NULL), the iteration is over.
                for (State v = edge.traverse(u); v != null; v = v.getNextResult()) {
                    
                    if ((this.skipTraverseResultStrategy != null)
                            && this.skipTraverseResultStrategy.shouldSkipTraversalResult(initialState.getVertex(), null, u, v,
                                    spt, this.options)) {
                        continue;
                    }
                    
                    if (this.verbose) {
                        System.out.printf("  w = %f + %f = %f %s", u.getWeight(), v.getWeightDelta(), v.getWeight(), v
                                .getVertex());
                    }

                    if (v.exceedsWeightLimit(this.options.maxWeight)) {
                        continue;
                    }
                    
                    if (spt.add(v)) {
                        double estimate = this.heuristic.computeForwardWeight(v, target);
                        queue.insert(v, v.getWeight() + estimate);
                    }
                    
                }
            }
            spt.postVisit(u);
        }
        return spt;
    }
    
    public void setHeuristic(RemainingWeightHeuristic heuristic) {
        this.heuristic = heuristic;
    }
}
