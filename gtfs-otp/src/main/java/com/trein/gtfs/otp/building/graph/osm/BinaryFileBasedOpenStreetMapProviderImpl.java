package com.trein.gtfs.otp.building.graph.osm;

import java.io.File;
import java.io.FileInputStream;

import com.trein.gtfs.otp.building.graph.OpenStreetMapContentHandler;
import com.trein.gtfs.otp.building.graph.OpenStreetMapProvider;

import crosby.binary.file.BlockInputStream;

/**
 * Parser for the OpenStreetMap PBF format. Parses files in three passes: First the relations, then
 * the ways, then the nodes are also loaded.
 *
 * @see http://wiki.openstreetmap.org/wiki/PBF_Format
 * @see org.opentripplanner.openstreetmap.services.graph_builder.services.osm.OpenStreetMapContentHandler#biPhase
 * @since 0.4
 */
public class BinaryFileBasedOpenStreetMapProviderImpl implements OpenStreetMapProvider {
    
    private File _path;
    
    @Override
    public void readOSM(OpenStreetMapContentHandler handler) {
        try {
            BinaryOpenStreetMapParser parser = new BinaryOpenStreetMapParser(handler);
            
            FileInputStream input = new FileInputStream(this._path);
            parser.setParseNodes(false);
            parser.setParseWays(false);
            (new BlockInputStream(input, parser)).process();
            
            handler.doneRelations();
            
            input = new FileInputStream(this._path);
            parser.setParseRelations(false);
            parser.setParseWays(true);
            (new BlockInputStream(input, parser)).process();
            
            handler.secondPhase();
            
            input = new FileInputStream(this._path);
            parser.setParseNodes(true);
            parser.setParseWays(false);
            (new BlockInputStream(input, parser)).process();
            handler.nodesLoaded();
        } catch (Exception ex) {
            throw new IllegalStateException("error loading OSM from path " + this._path, ex);
        }
    }
    
    public void setPath(File path) {
        this._path = path;
    }
    
    @Override
    public String toString() {
        return "BinaryFileBasedOpenStreetMapProviderImpl(" + this._path + ")";
    }
    
    @Override
    public void checkInputs() {
        if (!this._path.canRead()) { throw new RuntimeException("Can't read OSM path: " + this._path); }
    }
}
