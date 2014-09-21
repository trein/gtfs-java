package org.opentripplanner.standalone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class OSMDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(OSMDownloader.class);
    
    private double _latYStep = 0.04;
    
    private double _lonXStep = 0.04;
    
    private double _overlap = 0.001;
    
    private File _cacheDirectory;
    
    private int _updateIfOlderThanDuration = 0;
    
    private String _apiBaseUrl = "http://open.mapquestapi.com/xapi/api/0.6/";
    
    public void setLatStep(double latStep) {
        this._latYStep = latStep;
    }
    
    public void setLonStep(double lonStep) {
        this._lonXStep = lonStep;
    }
    
    public void setOverlap(double overlap) {
        this._overlap = overlap;
    }
    
    public void setCacheDirectory(File cacheDirectory) {
        this._cacheDirectory = cacheDirectory;
    }
    
    public void setUpdateIfOlderThanDuration(int durationInMilliseconds) {
        this._updateIfOlderThanDuration = durationInMilliseconds;
    }
    
    public void visitRegion(Envelope rectangle, OSMDownloaderListener listener) throws IOException {
        
        double minY = floor(rectangle.getMinY(), this._latYStep);
        double maxY = ceil(rectangle.getMaxY(), this._latYStep);
        double minX = floor(rectangle.getMinX(), this._lonXStep);
        double maxX = ceil(rectangle.getMaxX(), this._lonXStep);
        
        for (double y = minY; y < maxY; y += this._latYStep) {
            for (double x = minX; x < maxX; x += this._lonXStep) {
                String key = getKey(x, y);
                File path = getPathToUpToDateMapTile(y, x, key);
                try {
                    listener.handleMapTile(key, y, x, path);
                } catch (IllegalStateException e) {
                    LOG.debug("trying to re-download");
                    path.delete();
                    path = getPathToUpToDateMapTile(y, x, key);
                    listener.handleMapTile(key, y, x, path);
                }
            }
        }
    }
    
    public static double floor(double value, double step) {
        return step * Math.floor(value / step);
    }
    
    public static double ceil(double value, double step) {
        return step * Math.ceil(value / step);
    }
    
    private String formatNumberWithoutLocale(double number) {
        return String.format((Locale) null, "%.4f", number);
    }
    
    private String getKey(double x, double y) {
        return formatNumberWithoutLocale(y) + "_" + formatNumberWithoutLocale(x) + "_"
                + formatNumberWithoutLocale(this._latYStep) + "_" + formatNumberWithoutLocale(this._lonXStep) + "_"
                + formatNumberWithoutLocale(this._overlap);
    }
    
    private File getPathToUpToDateMapTile(double lat, double lon, String key) throws IOException {
        
        File path = getPathToMapTile(key);
        
        if (needsUpdate(path)) {
            Envelope r = new Envelope(lon - this._overlap, lon + this._lonXStep + this._overlap, lat - this._overlap, lat
                    + this._latYStep + this._overlap);
            
            LOG.debug("downloading osm tile: " + key + " from path " + path + " e " + path.exists());
            
            URL url = constructUrl(r);
            LOG.warn("downloading from " + url.toString());
            
            InputStream in = url.openStream();
            FileOutputStream out = new FileOutputStream(path);
            try {
                byte[] data = new byte[4096];
                while (true) {
                    int numBytes = in.read(data);
                    if (numBytes == -1) {
                        break;
                    }
                    out.write(data, 0, numBytes);
                }
                in.close();
                out.close();
            } catch (RuntimeException e) {
                out.close();
                LOG.info("Removing half-written file " + path);
                path.delete(); // clean up any half-written files
                throw e;
            }
        }
        
        return path;
    }
    
    private File getPathToMapTile(String key) throws IOException {
        if (this._cacheDirectory == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            this._cacheDirectory = new File(tmpDir, "osm-tiles");
        }
        
        if (!this._cacheDirectory.exists()) {
            if (!this._cacheDirectory.mkdirs()) { throw new RuntimeException("Failed to create directory " + this._cacheDirectory); }
        }
        
        File path = new File(this._cacheDirectory, "map-" + key + ".osm");
        return path;
    }
    
    private boolean needsUpdate(File path) {
        if (!path.exists()) { return true; }
        if (this._updateIfOlderThanDuration > 0) {
            if ((System.currentTimeMillis() - path.lastModified()) > this._updateIfOlderThanDuration) { return true; }
        }
        return false;
    }
    
    private URL constructUrl(Envelope r) {
        double left = r.getMinX();
        double right = r.getMaxX();
        double bottom = r.getMinY();
        double top = r.getMaxY();
        try {
            return new URL(getApiBaseUrl() + "map?bbox=" + left + "," + bottom + "," + right + "," + top);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Set the base OSM API URL from which OSM tiles will be downloaded.
     */
    public void setApiBaseUrl(String apiBaseUrl) {
        this._apiBaseUrl = apiBaseUrl;
    }
    
    public String getApiBaseUrl() {
        if (this._apiBaseUrl == null) { throw new IllegalStateException("Map API base URL must be set before building a URL."); }
        return this._apiBaseUrl;
    }
}
