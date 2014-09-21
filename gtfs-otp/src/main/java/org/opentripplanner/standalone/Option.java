package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Option {
    
    private static final Logger LOG = LoggerFactory.getLogger(Option.class);
    
    public List<Segment> transit;
    public List<StreetSegment> access;
    public List<StreetSegment> egress;
    public Stats stats = new Stats();
    public String summary;
    public List<DCFareCalculator.Fare> fares;
    
    // The fares are outside the transit segments because a fare can apply to multiple segments so
    // there is no one-to-one
    // correspondance. For example, when you transfer from one subway to another and pay one fare
    // for the two segments.
    
    public Option(Ride tail, Collection<StopAtDistance> accessPaths, Collection<StopAtDistance> egressPaths) {
        this.access = StreetSegment.list(accessPaths);
        this.egress = StreetSegment.list(egressPaths);
        // FIXME In the event that there is only access, N will still be 1 which is strange.
        this.stats.add(this.access); // FIXME double-adding access time here, it's already in the
                                     // path.
        this.stats.add(this.egress);
        List<Ride> rides = Lists.newArrayList();
        for (Ride ride = tail; ride != null; ride = ride.previous) {
            rides.add(ride);
        }
        if (!rides.isEmpty()) {
            Collections.reverse(rides);
            this.transit = Lists.newArrayList();
            for (Ride ride : rides) {
                Segment segment = new Segment(ride);
                this.transit.add(segment);
                this.stats.add(segment.walkTime);
                if (segment.waitStats != null) {
                    this.stats.add(segment.waitStats);
                }
                this.stats.add(segment.rideStats);
            }
        }
        // Really should be one per segment, with transfers to the same operator having a price of
        // 0.
        this.fares = DCFareCalculator.calculateFares(rides);
        this.summary = generateSummary();
    }
    
    /** Make a human readable text summary of this option. */
    public String generateSummary() {
        if ((this.transit == null) || this.transit.isEmpty()) { return "Non-transit options"; }
        List<String> vias = Lists.newArrayList();
        List<String> routes = Lists.newArrayList();
        for (Segment segment : this.transit) {
            List<String> routeShortNames = Lists.newArrayList();
            for (RouteShort rs : segment.routes) {
                String routeName = rs.shortName == null ? rs.longName : rs.shortName;
                routeShortNames.add(routeName);
            }
            routes.add(Joiner.on("/").join(routeShortNames));
            vias.add(segment.toName);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("routes ");
        sb.append(Joiner.on(", ").join(routes));
        if (!vias.isEmpty()) {
            vias.remove(vias.size() - 1);
        }
        if (!vias.isEmpty()) {
            sb.append(" via ");
            sb.append(Joiner.on(", ").join(vias));
        }
        return sb.toString();
    }
    
    public static enum SortOrder {
        MIN, AVG, MAX;
    }
    
    public static class MinComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.min - two.stats.min;
        }
    }
    
    public static class AvgComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.avg - two.stats.avg;
        }
    }
    
    public static class MaxComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.max - two.stats.max;
        }
    }
    
    /**
     * Rides or transfers may contain no patterns after applying time window.
     */
    public boolean hasEmptyRides() {
        for (Segment seg : this.transit) {
            if ((seg.rideStats.num == 0) || (seg.waitStats.num == 0)) { return true; }
        }
        return false;
    }
    
}
