package org.opentripplanner.standalone;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A ShortestPathTree implementation that corresponds to a basic Dijkstra search, where there is a
 * single optimal state per vertex. It maintains a closed vertex list since decrease-key operations
 * are not guaranteed to be supported by the priority queue.
 *
 * @author andrewbyrd
 */
public class BasicShortestPathTree extends AbstractShortestPathTree {

    private static final long serialVersionUID = MavenVersion.VERSION.getUID();

    private static final int DEFAULT_CAPACITY = 500;
    
    Map<Vertex, State> states;
    
    /**
     * Parameterless constructor that uses a default capacity for internal vertex-keyed data
     * structures.
     */
    public BasicShortestPathTree(RoutingRequest options) {
        this(options, DEFAULT_CAPACITY);
    }
    
    /**
     * Constructor with a parameter indicating the initial capacity of the data structures holding
     * vertices. This can help avoid resizing and rehashing these objects during path searches.
     *
     * @param n - the initial size of vertex-keyed maps
     */
    public BasicShortestPathTree(RoutingRequest options, int n) {
        super(options);
        this.states = new IdentityHashMap<Vertex, State>(n);
    }
    
    @Override
    public Collection<State> getAllStates() {
        return this.states.values();
    }
    
    /****
     * {@link ShortestPathTree} Interface
     ****/
    
    @Override
    public boolean add(State state) {
        Vertex here = state.getVertex();
        State existing = this.states.get(here);
        if ((existing == null) || state.betterThan(existing)) {
            this.states.put(here, state);
            return true;
        } else {
            final Edge backEdge = existing.getBackEdge();
            if (backEdge instanceof PlainStreetEdge) {
                PlainStreetEdge pseBack = (PlainStreetEdge) backEdge;
                if (pseBack.hasExplicitTurnRestrictions()) {
                    // If the previous back edge had turn restrictions, we need to continue
                    // the search because the previous path may be prevented by from reaching the
                    // end by turn restrictions.
                    return true;
                }
            }
            
            return false;
        }
    }
    
    @Override
    public List<State> getStates(Vertex dest) {
        State s = this.states.get(dest);
        if (s == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(s); // single-element array-backed list
        }
    }
    
    @Override
    public State getState(Vertex dest) {
        return this.states.get(dest);
    }
    
    @Override
    public boolean visit(State s) {
        final State existing = this.states.get(s.getVertex());
        final Edge backEdge = existing.getBackEdge();
        if (backEdge instanceof PlainStreetEdge) {
            PlainStreetEdge pseBack = (PlainStreetEdge) backEdge;
            if (pseBack.hasExplicitTurnRestrictions()) {
                // If the previous back edge had turn restrictions, we need to continue
                // the search because the previous path may be prevented by from reaching the end by
                // turn restrictions.
                return true;
            }
        }
        return (s == existing);
    }
    
    @Override
    public int getVertexCount() {
        return this.states.size();
    }
    
}
