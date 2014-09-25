package com.trein.gtfs.otp.building.graph.osm;

import com.trein.gtfs.otp.building.graph.osm.model.OSMWithTags;

public class NoteProperties {
    
    public String notePattern;
    
    public NoteProperties(String notePattern) {
        this.notePattern = notePattern;
    }
    
    public NoteProperties() {
        this.notePattern = null;
    }
    
    public String generateNote(OSMWithTags way) {
        return TemplateLibrary.generate(this.notePattern, way);
    }
}
