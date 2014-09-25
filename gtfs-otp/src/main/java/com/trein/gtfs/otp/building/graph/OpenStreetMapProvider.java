package com.trein.gtfs.otp.building.graph;

public interface OpenStreetMapProvider {
    void readOSM(OpenStreetMapContentHandler handler);
    
    void checkInputs();
}
