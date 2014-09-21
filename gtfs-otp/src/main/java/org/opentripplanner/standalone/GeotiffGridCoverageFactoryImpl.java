package org.opentripplanner.standalone;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

/**
 * Implementation of ElevationGridCoverageFactory for Geotiff data.
 */
public class GeotiffGridCoverageFactoryImpl implements ElevationGridCoverageFactory {
    
    private File path = null;
    private GridCoverage2D coverage;
    
    public GeotiffGridCoverageFactoryImpl() {
        
    }
    
    public GeotiffGridCoverageFactoryImpl(File path) {
        this.path = path;
    }
    
    public void setPath(File path) {
        this.path = path;
    }
    
    @Override
    public GridCoverage2D getGridCoverage() {
        GeoTiffFormat format = new GeoTiffFormat();
        GeoTiffReader reader = null;
        
        try {
            if (this.path == null) { throw new RuntimeException("Path not set"); }
            reader = format.getReader(this.path);
            this.coverage = reader.read(null);
        } catch (IOException e) {
            throw new RuntimeException("Error getting coverage automatically. ", e);
        }
        
        return this.coverage;
    }
    
    @Override
    public void checkInputs() {
        if (!this.path.canRead()) { throw new RuntimeException("Can't read elevation path: " + this.path); }
    }
    
    @Override
    public void setGraph(Graph graph) {
        // nothing to do here
    }
    
}
