package org.opentripplanner.standalone;

import java.util.List;

import org.onebusaway.gtfs.model.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Groups stops by geographic proximity and name similarity. This will at least half the number of
 * distinct stop places. In profile routing this means a lot less branching and a lot less transfers
 * to consider. It seems to work quite well for both the Washington DC region and Portland.
 * Locations outside the US would require additional stop name normalizer modules.
 */
public class StopCluster {
    
    private static final Logger LOG = LoggerFactory.getLogger(StopCluster.class);
    
    public final String id;
    public final String name;
    public double lon;
    public double lat;
    public final List<Stop> children = Lists.newArrayList();
    
    public StopCluster(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public void computeCenter() {
        double lonSum = 0, latSum = 0;
        for (Stop stop : this.children) {
            lonSum += stop.getLon();
            latSum += stop.getLat();
        }
        this.lon = lonSum / this.children.size();
        this.lat = latSum / this.children.size();
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
}
