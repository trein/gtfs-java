package org.opentripplanner.standalone;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Common base class for common {@link ShortestPathTree} functionality.
 *
 * @author bdferris
 */
public abstract class AbstractShortestPathTree implements ShortestPathTree {
    
    public final RoutingRequest options;

    protected AbstractShortestPathTree() {
        this.options = null;
    }
    
    protected AbstractShortestPathTree(RoutingRequest options) {
        this.options = options;
    }

    @Override
    public List<GraphPath> getPaths() {
        return getPaths(this.options.getRoutingContext().target, true);
    }

    @Override
    public List<GraphPath> getPaths(Vertex dest, boolean optimize) {
        List<? extends State> stateList = getStates(dest);
        if (stateList == null) { return Collections.emptyList(); }
        List<GraphPath> ret = new LinkedList<GraphPath>();
        for (State s : stateList) {
            if (s.isFinal() && s.allPathParsersAccept()) {
                ret.add(new GraphPath(s, optimize));
            }
        }
        return ret;
    }
    
    @Override
    public GraphPath getPath(Vertex dest, boolean optimize) {
        State s = getState(dest);
        if (s == null) {
            return null;
        } else {
            return new GraphPath(s, optimize);
        }
    }
    
    @Override
    public void postVisit(State u) {
    }

    @Override
    public RoutingRequest getOptions() {
        return this.options;
    }
    
}
