package com.trein.gtfs.otp.building.graph.osm.model;

public class OSMTag {
    
    private String k;
    private String v;
    
    public OSMTag() {
    }
    
    public OSMTag(String k, String v) {
        this.k = k;
        this.v = v;
    }
    
    public String getK() {
        return this.k;
    }
    
    public void setK(String k) {
        this.k = k;
    }
    
    public String getV() {
        return this.v;
    }
    
    public void setV(String v) {
        this.v = v;
    }
    
    @Override
    public String toString() {
        return this.k + "=" + this.v;
    }
}
