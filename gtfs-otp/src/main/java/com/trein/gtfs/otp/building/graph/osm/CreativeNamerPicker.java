package com.trein.gtfs.otp.building.graph.osm;

import com.trein.gtfs.otp.building.graph.osm.model.OSMSpecifier;

/**
 * Describes how unnamed OSM ways are to be named based on the tags they possess. The CreativeNamer
 * will be applied to ways that match the OSMSpecifier.
 * 
 * @author novalis
 */
public class CreativeNamerPicker {
    public OSMSpecifier specifier;
    public CreativeNamer namer;

    public CreativeNamerPicker() {
        this.specifier = null;
        this.namer = null;
    }

    public CreativeNamerPicker(OSMSpecifier specifier, CreativeNamer namer) {
        this.specifier = specifier;
        this.namer = namer;
    }
}
