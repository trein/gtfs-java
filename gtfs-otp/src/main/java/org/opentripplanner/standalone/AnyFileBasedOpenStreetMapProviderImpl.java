package org.opentripplanner.standalone;

import java.io.File;

public class AnyFileBasedOpenStreetMapProviderImpl implements OpenStreetMapProvider {
    
    private File _path;
    
    public void setPath(File path) {
        this._path = path;
    }
    
    public AnyFileBasedOpenStreetMapProviderImpl(File file) {
        this.setPath(file);
    }

    public AnyFileBasedOpenStreetMapProviderImpl() {
    };
    
    @Override
    public void readOSM(OpenStreetMapContentHandler handler) {
        try {
            if (this._path.getName().endsWith(".pbf")) {
                BinaryFileBasedOpenStreetMapProviderImpl p = new BinaryFileBasedOpenStreetMapProviderImpl();
                p.setPath(this._path);
                p.readOSM(handler);
            } else {
                StreamedFileBasedOpenStreetMapProviderImpl p = new StreamedFileBasedOpenStreetMapProviderImpl();
                p.setPath(this._path);
                p.readOSM(handler);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("error loading OSM from path " + this._path, ex);
        }
    }
    
    @Override
    public String toString() {
        return "AnyFileBasedOpenStreetMapProviderImpl(" + this._path + ")";
    }
    
    @Override
    public void checkInputs() {
        if (!this._path.canRead()) { throw new RuntimeException("Can't read OSM path: " + this._path); }
    }
}
