package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

import com.beust.jcommander.internal.Lists;

public class RouteShort {
    
    public AgencyAndId id;
    public String shortName;
    public String longName;
    public String mode;
    public String color;

    public RouteShort(Route route) {
        this.id = route.getId();
        this.shortName = route.getShortName();
        this.longName = route.getLongName();
        this.mode = GtfsLibrary.getTraverseMode(route).toString();
        this.color = route.getColor();
    }

    public static List<RouteShort> list(Collection<Route> in) {
        List<RouteShort> out = Lists.newArrayList();
        for (Route route : in) {
            out.add(new RouteShort(route));
        }
        return out;
    }
    
}
