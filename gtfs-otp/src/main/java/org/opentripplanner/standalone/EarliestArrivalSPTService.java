package org.opentripplanner.standalone;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute full SPT for earliest arrival problem. Always builds a full shortest path tree
 * ("batch mode"). Note that walk limiting must be turned off -- resource limiting is not
 * algorithmically correct.
 */
public class EarliestArrivalSPTService implements SPTService {
    
    private static final Logger LOG = LoggerFactory.getLogger(EarliestArrivalSPTService.class);
    
    public int maxDuration = 60 * 60 * 2;
    
    @Override
    public ShortestPathTree getShortestPathTree(RoutingRequest req) {
        return getShortestPathTree(req, -1, null); // negative timeout means no timeout
    }

    @Override
    public ShortestPathTree getShortestPathTree(RoutingRequest req, double timeoutSeconds) {
        return this.getShortestPathTree(req, timeoutSeconds, null);
    }
    
    public ShortestPathTree getShortestPathTree(RoutingRequest options, double relTimeout,
            SearchTerminationStrategy terminationStrategy) {

        // clone options before modifying, otherwise disabling resource limiting will cause
        // SPT cache misses for subsequent requests.
        options = options.clone();

        // disable any resource limiting, which is algorithmically invalid here
        options.setMaxTransfers(Integer.MAX_VALUE);
        options.setMaxWalkDistance(Double.MAX_VALUE);
        if (options.clampInitialWait < 0) {
            options.clampInitialWait = (60 * 30);
        }

        // impose search cutoff
        final long maxt = this.maxDuration + options.clampInitialWait;
        options.worstTime = options.dateTime + (options.arriveBy ? -maxt : maxt);
        
        // SPT cache does not look at routing request in SPT to perform lookup,
        // so it's OK to construct with the local cloned one
        ShortestPathTree spt = new EarliestArrivalShortestPathTree(options);
        State initialState = new State(options);
        spt.add(initialState);
        
        BinHeap<State> pq = new BinHeap<State>();
        pq.insert(initialState, 0);
        
        while (!pq.empty()) {
            State u = pq.extract_min();
            Vertex u_vertex = u.getVertex();
            if (!spt.visit(u)) {
                continue;
            }
            Collection<Edge> edges = options.arriveBy ? u_vertex.getIncoming() : u_vertex.getOutgoing();
            for (Edge edge : edges) {
                for (State v = edge.traverse(u); v != null; v = v.getNextResult()) {
                    if (isWorstTimeExceeded(v, options)) {
                        continue;
                    }
                    if (spt.add(v)) {
                        pq.insert(v, v.getActiveTime()); // activeTime?
                    }
                }
            }
        }
        return spt;
    }
    
    // Move this into State
    private boolean isWorstTimeExceeded(State v, RoutingRequest opt) {
        if (opt.arriveBy) {
            return v.getTimeSeconds() < opt.worstTime;
        } else {
            return v.getTimeSeconds() > opt.worstTime;
        }
    }
    
}
