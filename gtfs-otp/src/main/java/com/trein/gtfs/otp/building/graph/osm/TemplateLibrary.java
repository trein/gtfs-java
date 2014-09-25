package com.trein.gtfs.otp.building.graph.osm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trein.gtfs.otp.building.graph.osm.model.OSMWithTags;

public class TemplateLibrary {
    private static final Pattern patternMatcher = Pattern.compile("\\{(.*?)\\}");
    
    public static String generate(String pattern, OSMWithTags way) {
        
        if (pattern == null) { return null; }
        StringBuffer gen_name = new StringBuffer();
        
        Matcher matcher = patternMatcher.matcher(pattern);

        int lastEnd = 0;
        while (matcher.find()) {
            // add the stuff before the match
            gen_name.append(pattern, lastEnd, matcher.start());
            lastEnd = matcher.end();
            // and then the value for the match
            String key = matcher.group(1);
            String tag = way.getTag(key);
            if (tag != null) {
                gen_name.append(tag);
            }
        }
        gen_name.append(pattern, lastEnd, pattern.length());
        
        return gen_name.toString();
    }
}
