package org.opentripplanner.standalone;


/**
 * A superclass for general trip pattern related edges
 * 
 * @author novalis
 */
public abstract class TablePatternEdge extends Edge implements PatternEdge {
    
    private static final long serialVersionUID = 1L;
    
    public TablePatternEdge(TransitVertex fromv, TransitVertex tov) {
        super(fromv, tov);
    }
    
    public TripPattern getPattern() {
        return ((OnboardVertex) this.fromv).getTripPattern();
    }
    
    public abstract int getStopIndex();
    
}
