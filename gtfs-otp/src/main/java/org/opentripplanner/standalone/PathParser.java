package org.opentripplanner.standalone;


public abstract class PathParser {
    
    public int transition(int initState, int terminal) {
        return this.getDFA().transition(initState, terminal);
    }
    
    public boolean accepts(int parseState) {
        return this.getDFA().accepts(parseState);
    }
    
    /**
     * Concrete PathParsers implement this method to convert OTP States (and their backEdges) into
     * terminals in the language they define. This method will only be called on States that are the
     * result of edge transitions, never on States when they are constructed directly as initial
     * states. This fact circumvents the problem that initial states have no back edge.
     */
    public abstract int terminalFor(State state);
    
    /**
     * Concrete PathParsers implement this method to provide a DFA that will accept certain paths
     * and not others.
     */
    protected abstract DFA getDFA();
    
}
