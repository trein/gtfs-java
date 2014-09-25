package com.trein.gtfs.otp.building.graph.osm;

import com.trein.gtfs.otp.building.graph.osm.model.OSMSpecifier;

/**
 * Defines which OSM ways get notes and what kind of notes they get.
 *
 * @author novalis
 */
public class NotePicker {
    public OSMSpecifier specifier;
    public NoteProperties noteProperties;
    
    public NotePicker(OSMSpecifier specifier, NoteProperties noteProperties) {
        this.specifier = specifier;
        this.noteProperties = noteProperties;
    }
}
