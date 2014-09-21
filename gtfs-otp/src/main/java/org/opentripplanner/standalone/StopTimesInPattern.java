package org.opentripplanner.standalone;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Some stopTimes all in the same pattern. TripTimeShort should probably be renamed StopTimeShort
 */
public class StopTimesInPattern {
    
    public PatternShort pattern;
    public List<TripTimeShort> times = Lists.newArrayList();
    
    public StopTimesInPattern(TripPattern pattern) {
        this.pattern = new PatternShort(pattern);
    }
    
}
