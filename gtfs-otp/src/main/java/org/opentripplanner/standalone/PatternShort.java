package org.opentripplanner.standalone;

import java.util.Collection;
import java.util.List;

import com.beust.jcommander.internal.Lists;

public class PatternShort {
    
    public String id;
    public String desc;

    public PatternShort(TripPattern pattern) {
        this.id = pattern.code;
        this.desc = pattern.name;
    }

    public static List<PatternShort> list(Collection<TripPattern> in) {
        List<PatternShort> out = Lists.newArrayList();
        for (TripPattern pattern : in) {
            out.add(new PatternShort(pattern));
        }
        return out;
    }
    
}
