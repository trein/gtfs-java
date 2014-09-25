package com.trein.gtfs.otp.building.graph.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import com.trein.gtfs.otp.building.graph.OpenStreetMapContentHandler;
import com.trein.gtfs.otp.building.graph.OpenStreetMapProvider;

/**
 * @author Vincent Privat
 * @since 1.0
 */
public class StreamedFileBasedOpenStreetMapProviderImpl implements OpenStreetMapProvider {

    private File _path;

    @Override
    public void readOSM(OpenStreetMapContentHandler handler) {
        try {
            if (this._path.getName().endsWith(".gz")) {
                InputStream in = new GZIPInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 1);

                handler.doneRelations();

                in = new GZIPInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 2);

                handler.secondPhase();

                in = new GZIPInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 3);

                handler.nodesLoaded();
            } else if (this._path.getName().endsWith(".bz2")) {
                InputStream in = new BZip2CompressorInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 1);

                handler.doneRelations();

                in = new BZip2CompressorInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 2);

                handler.secondPhase();

                in = new BZip2CompressorInputStream(new FileInputStream(this._path));
                StreamedOpenStreetMapParser.parseMap(in, handler, 3);
                
                handler.nodesLoaded();
            } else {
                StreamedOpenStreetMapParser.parseMap(this._path, handler);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("error loading OSM from path " + this._path, ex);
        }
    }

    public void setPath(File path) {
        this._path = path;
    }

    @Override
    public String toString() {
        return "StreamedFileBasedOpenStreetMapProviderImpl(" + this._path + ")";
    }

    @Override
    public void checkInputs() {
        if (!this._path.canRead()) { throw new RuntimeException("Can't read OSM path: " + this._path); }
    }
}
