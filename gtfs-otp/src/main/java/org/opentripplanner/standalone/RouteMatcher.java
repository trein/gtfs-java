package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.HashSet;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

/**
 * A RouteMatcher is a collection of routes based on IDs, short name and/or agency IDs. We currently
 * support route full IDs (agency ID + route ID), agency ID + route name, or route name only.
 * Support for other matching expression can be easily added later on.
 */
public class RouteMatcher implements Cloneable, Serializable {
    private static final long serialVersionUID = 8066547338465440312L;
    
    /* Set of full matching route ids (agency ID + route ID) */
    private final HashSet<AgencyAndId> agencyAndRouteIds = new HashSet<AgencyAndId>();
    
    /* Set of full matching route code/names (agency ID + route code/name) */
    private final HashSet<T2<String, String>> agencyIdAndRouteNames = new HashSet<T2<String, String>>();
    
    /* Set of matching route names (without specifying an agency ID) */
    private final HashSet<String> routeNames = new HashSet<String>();
    
    private static RouteMatcher EMPTY_MATCHER = new RouteMatcher();
    
    private RouteMatcher() {
    }
    
    /**
     * Return an empty matcher (which match no routes).
     */
    public static RouteMatcher emptyMatcher() {
        return EMPTY_MATCHER;
    }
    
    /**
     * Build a new RouteMatcher from a string representation.
     *
     * @param routeSpecList A comma-separated list of route spec, each of the format
     *        [agencyId]_[routeName]_[routeId] Please note that this format is not really intuitive
     *        as it does not follow the OBA-gtfslib AgencyAndId standard ('agencyID_routeId'). This
     *        was kept for backward-compatibility purposes. If the original routeName contains some
     *        "_" each *must* be replaced by a space. If the agency or route ID contains a "_" they
     *        must be escaped using a backslash. TODO why do we want to accept route name strings
     *        when we have IDs? Break backward compatibility. FIXME this is the only place we are
     *        still using underscores as scope separators. Rethink this from scratch.
     * @return A RouteMatcher
     * @throws IllegalArgumentException If the string representation is invalid.
     */
    public static RouteMatcher parse(String routeSpecList) {
        if (routeSpecList == null) { return emptyMatcher(); }
        RouteMatcher retval = new RouteMatcher();
        int n = 0;
        for (String element : routeSpecList.split(",")) {
            if (element.length() == 0) {
                continue;
            }
            n++;
            // FIXME regexes with no comments
            String[] routeSpec = element.split("(?<!\\\\)_", 3);
            if ((routeSpec.length != 2) && (routeSpec.length != 3)) { throw new IllegalArgumentException(
                    "Wrong route spec format: " + element); }
            routeSpec[0] = routeSpec[0].replace("\\_", "_");
            routeSpec[1] = routeSpec[1].replace("\\_", " ");
            if (routeSpec.length >= 3) {
                routeSpec[2] = routeSpec[2].replace("\\_", "_");
            }
            String agencyId = routeSpec[0];
            if (agencyId.length() == 0) {
                agencyId = null;
            }
            String routeName = routeSpec[1];
            if (routeName.length() == 0) {
                routeName = null;
            }
            String routeId = routeSpec.length > 2 ? routeSpec[2] : null;
            if ((routeId != null) && (routeId.length() == 0)) {
                routeId = null;
            }
            if ((agencyId != null) && (routeId != null) && (routeName == null)) {
                // Case 1: specified agency ID and route ID but no route name
                retval.agencyAndRouteIds.add(new AgencyAndId(agencyId, routeId));
            } else if ((agencyId != null) && (routeName != null) && (routeId == null)) {
                // Case 2: specified agency ID and route name but no route ID
                retval.agencyIdAndRouteNames.add(new T2<String, String>(agencyId, routeName));
            } else if ((agencyId == null) && (routeName != null) && (routeId == null)) {
                // Case 3: specified route name only
                retval.routeNames.add(routeName);
            } else {
                throw new IllegalArgumentException("Wrong route spec format: " + element);
            }
        }
        if (n == 0) { return emptyMatcher(); }
        return retval;
    }
    
    public boolean matches(Route route) {
        if (this == EMPTY_MATCHER) { return false; }
        if (this.agencyAndRouteIds.contains(route.getId())) { return true; }
        String routeName = GtfsLibrary.getRouteName(route).replace("_", " ");
        if (this.agencyIdAndRouteNames.contains(new T2<String, String>(route.getId().getAgencyId(), routeName))) { return true; }
        if (this.routeNames.contains(routeName)) { return true; }
        return false;
    }
    
    public String asString() {
        StringBuilder builder = new StringBuilder();
        for (AgencyAndId agencyAndId : this.agencyAndRouteIds) {
            builder.append(agencyAndId.getAgencyId() + "__" + agencyAndId.getId());
            builder.append(",");
        }
        for (T2<String, String> agencyIdAndRouteName : this.agencyIdAndRouteNames) {
            builder.append(agencyIdAndRouteName.getFirst() + "_" + agencyIdAndRouteName.getSecond());
            builder.append(",");
        }
        for (String routeName : this.routeNames) {
            builder.append("_" + routeName);
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return String.format("RouteMatcher<agencyAndRouteIds=%s agencyIdAndRouteNames=%s routeNames=%s>", this.agencyAndRouteIds,
                this.agencyIdAndRouteNames, this.routeNames);
    }
    
    @Override
    public boolean equals(Object another) {
        if ((another == null) || !(another instanceof RouteMatcher)) { return false; }
        if (another == this) { return true; }
        RouteMatcher anotherMatcher = (RouteMatcher) another;
        return this.agencyAndRouteIds.equals(anotherMatcher.agencyAndRouteIds)
                && this.agencyIdAndRouteNames.equals(anotherMatcher.agencyIdAndRouteNames)
                && this.routeNames.equals(anotherMatcher.routeNames);
    }
    
    @Override
    public int hashCode() {
        return this.agencyAndRouteIds.hashCode() + this.agencyIdAndRouteNames.hashCode() + this.routeNames.hashCode();
    }
    
    @Override
    public RouteMatcher clone() {
        try {
            return (RouteMatcher) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }
}
