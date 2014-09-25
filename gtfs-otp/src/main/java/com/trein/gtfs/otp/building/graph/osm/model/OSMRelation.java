package com.trein.gtfs.otp.building.graph.osm.model;

import java.util.ArrayList;
import java.util.List;

public class OSMRelation extends OSMWithTags {
    
    private final List<OSMRelationMember> _members = new ArrayList<OSMRelationMember>();
    
    public void addMember(OSMRelationMember member) {
        this._members.add(member);
    }
    
    public List<OSMRelationMember> getMembers() {
        return this._members;
    }
    
    @Override
    public String toString() {
        return "osm relation " + this.id;
    }
}
