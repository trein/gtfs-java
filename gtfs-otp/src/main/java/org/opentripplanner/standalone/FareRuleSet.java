package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;

public class FareRuleSet implements Serializable {
    
    private static final long serialVersionUID = 7218355718876553028L;
    
    private final Set<AgencyAndId> routes;
    private final Set<P2<String>> originDestinations;
    private final Set<String> contains;

    public FareRuleSet() {
        this.routes = new HashSet<AgencyAndId>();
        this.originDestinations = new HashSet<P2<String>>();
        this.contains = new HashSet<String>();
    }
    
    public void addOriginDestination(String origin, String destination) {
        this.originDestinations.add(new P2<String>(origin, destination));
    }
    
    public void addContains(String containsId) {
        this.contains.add(containsId);
    }

    public void addRoute(AgencyAndId route) {
        this.routes.add(route);
    }
    
    public boolean matches(String startZone, String endZone, Set<String> zonesVisited, Set<AgencyAndId> routesVisited) {
        // check for matching origin/destination, if this ruleset has any origin/destination
        // restrictions
        if (this.originDestinations.size() > 0) {
            P2<String> od = new P2<String>(startZone, endZone);
            if (!this.originDestinations.contains(od)) {
                P2<String> od2 = new P2<String>(od.getFirst(), null);
                if (!this.originDestinations.contains(od2)) {
                    od2 = new P2<String>(null, od.getFirst());
                    if (!this.originDestinations.contains(od2)) { return false; }
                }
            }
        }
        
        // check for matching contains, if this ruleset has any containment restrictions
        if (this.contains.size() > 0) {
            if (!zonesVisited.equals(this.contains)) { return false; }
        }
        
        // check for matching routes
        if (this.routes.size() != 0) {
            if (!this.routes.containsAll(routesVisited)) { return false; }
        }
        
        return true;
    }
}
