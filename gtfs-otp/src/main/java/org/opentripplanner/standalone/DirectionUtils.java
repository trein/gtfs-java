package org.opentripplanner.standalone;

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class DirectionUtils {

    public static DirectionUtils instance;
    private static DistanceLibrary distanceLibrary = SphericalDistanceLibrary.getInstance();
    /* this is used to calculate angles on a sphere */
    private final GeodeticCalculator geodeticCalculator;

    private DirectionUtils() {
        // TODO(flamholz): Is constructing GeodeticCalculator really so
        // heavyweight that we need this synchronization?
        this.geodeticCalculator = new GeodeticCalculator();
    }

    private static synchronized DirectionUtils getInstance() {
        if (instance == null) {
            instance = new DirectionUtils();
        }
        return instance;
    }

    /**
     * Returns the azimuth in decimal degrees from (-180° to +180°) between Coordinates A and B.
     *
     * @param a
     * @param b
     * @return
     */
    public static synchronized double getAzimuth(Coordinate a, Coordinate b) {
        DirectionUtils utils = getInstance();
        utils.geodeticCalculator.setStartingGeographicPoint(a.x, a.y);
        utils.geodeticCalculator.setDestinationGeographicPoint(b.x, b.y);
        return utils.geodeticCalculator.getAzimuth();
    }
    
    /**
     * Computes the angle of the last segment of a LineString or MultiLineString
     *
     * @param geometry a LineString or a MultiLineString
     * @return
     */
    public static synchronized double getLastAngle(Geometry geometry) {
        LineString line;
        if (geometry instanceof MultiLineString) {
            line = (LineString) geometry.getGeometryN(geometry.getNumGeometries() - 1);
        } else {
            assert geometry instanceof LineString;
            line = (LineString) geometry;
        }
        int numPoints = line.getNumPoints();
        Coordinate coord0 = line.getCoordinateN(numPoints - 2);
        Coordinate coord1 = line.getCoordinateN(numPoints - 1);
        int i = numPoints - 3;
        int minDistance = 10; // Meters
        while ((distanceLibrary.fastDistance(coord0, coord1) < minDistance) && (i >= 0)) {
            coord0 = line.getCoordinateN(i--);
        }

        DirectionUtils utils = getInstance();
        utils.geodeticCalculator.setStartingGeographicPoint(coord0.x, coord0.y);
        utils.geodeticCalculator.setDestinationGeographicPoint(coord1.x, coord1.y);
        double az = utils.geodeticCalculator.getAzimuth();
        return (az * Math.PI) / 180;
    }

    /**
     * Computes the angle of the first segment of a LineString or MultiLineString
     *
     * @param geometry a LineString or a MultiLineString
     * @return
     */
    public static synchronized double getFirstAngle(Geometry geometry) {
        LineString line;
        if (geometry instanceof MultiLineString) {
            line = (LineString) geometry.getGeometryN(0);
        } else {
            assert geometry instanceof LineString;
            line = (LineString) geometry;
        }

        Coordinate coord0 = line.getCoordinateN(0);
        Coordinate coord1 = line.getCoordinateN(1);
        int i = 2;
        int minDistance = 10; // Meters
        while ((distanceLibrary.fastDistance(coord0, coord1) < minDistance) && (i < line.getNumPoints())) {
            coord1 = line.getCoordinateN(i++);
        }

        DirectionUtils utils = getInstance();
        utils.geodeticCalculator.setStartingGeographicPoint(coord0.x, coord0.y);
        utils.geodeticCalculator.setDestinationGeographicPoint(coord1.x, coord1.y);
        double az = utils.geodeticCalculator.getAzimuth();
        return (az * Math.PI) / 180;
    }
}
