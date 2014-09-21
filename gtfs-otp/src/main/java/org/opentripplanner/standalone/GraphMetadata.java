package org.opentripplanner.standalone;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

@XmlRootElement
public class GraphMetadata {
    
    /** The bounding box of the graph, in decimal degrees. */
    private double lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude;
    
    private HashSet<TraverseMode> transitModes = new HashSet<TraverseMode>();
    
    private double centerLatitude;
    
    private double centerLongitude;
    
    public GraphMetadata() {
        // 0-arg constructor avoids com.sun.xml.bind.v2.runtime.IllegalAnnotationsException
    }
    
    public GraphMetadata(Graph graph) {
        /* generate extents */
        Envelope leftEnv = new Envelope();
        Envelope rightEnv = new Envelope();
        double aRightCoordinate = 0;
        for (Vertex v : graph.getVertices()) {
            for (Edge e : v.getOutgoing()) {
                if (e instanceof PatternHop) {
                    this.transitModes.add(((PatternHop) e).getMode());
                }
            }
            Coordinate c = v.getCoordinate();
            if (c.x < 0) {
                leftEnv.expandToInclude(c);
            } else {
                rightEnv.expandToInclude(c);
                aRightCoordinate = c.x;
            }
        }
        
        if (leftEnv.getArea() == 0) {
            // the entire area is in the eastern hemisphere
            setLowerLeftLongitude(rightEnv.getMinX());
            setUpperRightLongitude(rightEnv.getMaxX());
            setLowerLeftLatitude(rightEnv.getMinY());
            setUpperRightLatitude(rightEnv.getMaxY());
        } else if (rightEnv.getArea() == 0) {
            // the entire area is in the western hemisphere
            setLowerLeftLongitude(leftEnv.getMinX());
            setUpperRightLongitude(leftEnv.getMaxX());
            setLowerLeftLatitude(leftEnv.getMinY());
            setUpperRightLatitude(leftEnv.getMaxY());
        } else {
            // the area spans two hemispheres. Either it crosses the prime meridian,
            // or it crosses the 180th meridian (roughly, the international date line). We'll check
            // a random
            // coordinate to find out
            
            if (aRightCoordinate < 90) {
                // assume prime meridian
                setLowerLeftLongitude(leftEnv.getMinX());
                setUpperRightLongitude(rightEnv.getMaxX());
            } else {
                // assume 180th meridian
                setLowerLeftLongitude(leftEnv.getMaxX());
                setUpperRightLongitude(rightEnv.getMinX());
            }
            setUpperRightLatitude(Math.max(rightEnv.getMaxY(), leftEnv.getMaxY()));
            setLowerLeftLatitude(Math.min(rightEnv.getMinY(), leftEnv.getMinY()));
        }
        // Does not work around 180th parallel.
        // Should be replaced by using k-means center code from TransitIndex, and storing the center
        // directly in the graph.
        setCenterLatitude((this.upperRightLatitude + this.lowerLeftLatitude) / 2);
        setCenterLongitude((this.upperRightLongitude + this.lowerLeftLongitude) / 2);
    }
    
    public void setLowerLeftLatitude(double lowerLeftLatitude) {
        this.lowerLeftLatitude = lowerLeftLatitude;
    }
    
    public double getLowerLeftLatitude() {
        return this.lowerLeftLatitude;
    }
    
    public void setUpperRightLatitude(double upperRightLatitude) {
        this.upperRightLatitude = upperRightLatitude;
    }
    
    public double getUpperRightLatitude() {
        return this.upperRightLatitude;
    }
    
    public void setUpperRightLongitude(double upperRightLongitude) {
        this.upperRightLongitude = upperRightLongitude;
    }
    
    public double getUpperRightLongitude() {
        return this.upperRightLongitude;
    }
    
    public void setLowerLeftLongitude(double lowerLeftLongitude) {
        this.lowerLeftLongitude = lowerLeftLongitude;
    }
    
    public double getLowerLeftLongitude() {
        return this.lowerLeftLongitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public void setMinLatitude(double minLatitude) {
        this.lowerLeftLatitude = minLatitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public double getMinLatitude() {
        return this.lowerLeftLatitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public void setMinLongitude(double minLongitude) {
        this.lowerLeftLongitude = minLongitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public double getMinLongitude() {
        return this.lowerLeftLongitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public void setMaxLatitude(double maxLatitude) {
        this.upperRightLatitude = maxLatitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public double getMaxLatitude() {
        return this.upperRightLatitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public void setMaxLongitude(double maxLongitude) {
        this.upperRightLongitude = maxLongitude;
    }
    
    /**
     * The bounding box of the graph, in decimal degrees. These are the old, deprecated names; the
     * new names are the lowerLeft/upperRight.
     * 
     * @deprecated
     */
    @Deprecated
    public double getMaxLongitude() {
        return this.upperRightLongitude;
    }
    
    @XmlElement
    public HashSet<TraverseMode> getTransitModes() {
        return this.transitModes;
    }
    
    public void setTransitModes(HashSet<TraverseMode> transitModes) {
        this.transitModes = transitModes;
    }
    
    public double getCenterLongitude() {
        return this.centerLongitude;
    }
    
    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }
    
    public double getCenterLatitude() {
        return this.centerLatitude;
    }
    
    public void setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }
}
