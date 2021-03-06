package com.trein.gtfs.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Rules for drawing lines on a map to represent a transit organization's routes.
 *
 * @author trein
 */
@Entity(name = "shapes")
@Table(indexes = { @Index(name = "o_shape_idx", columnList = "o_shape_id") })
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Shape implements Comparable<Shape> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "o_shape_id", nullable = false)
    private String shapeId;

    @Column(name = "sequence")
    private long sequence;

    @Column(name = "distance")
    private double distanceTraveled;
    
    private Location location;

    Shape() {
        
    }
    
    public Shape(String shapeId, Location location, long sequence, double distanceTraveled) {
        this.shapeId = shapeId;
        this.location = location;
        this.sequence = sequence;
        this.distanceTraveled = distanceTraveled;
    }

    public long getId() {
        return this.id;
    }

    /**
     * shape_id Required The shape_id field contains an ID that uniquely identifies a shape.
     */
    public String getShapeId() {
        return this.shapeId;
    }

    /**
     * shape_pt_lat Required The shape_pt_lat field associates a shape point's latitude with a shape
     * ID. The field value must be a valid WGS 84 latitude. Each row in shapes.txt represents a
     * shape point in your shape definition. For example, if the shape "A_shp" has three points in
     * its definition, the shapes.txt file might contain these rows to define the shape:
     *
     * <pre>
     * A_shp,37.61956,-122.48161,0
     * A_shp,37.64430,-122.41070,6
     * A_shp,37.65863,-122.30839,11
     * </pre>
     *
     * shape_pt_lon Required The shape_pt_lon field associates a shape point's longitude with a
     * shape ID. The field value must be a valid WGS 84 longitude value from -180 to 180. Each row
     * in shapes.txt represents a shape point in your shape definition. For example, if the shape
     * "A_shp" has three points in its definition, the shapes.txt file might contain these rows to
     * define the shape:
     *
     * <pre>
     * A_shp,37.61956,-122.48161,0
     * A_shp,37.64430,-122.41070,6
     * A_shp,37.65863,-122.30839,11
     * </pre>
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * shape_pt_sequence Required The shape_pt_sequence field associates the latitude and longitude
     * of a shape point with its sequence order along the shape. The values for shape_pt_sequence
     * must be non-negative integers, and they must increase along the trip. For example, if the
     * shape "A_shp" has three points in its definition, the shapes.txt file might contain these
     * rows to define the shape:
     *
     * <pre>
     * A_shp,37.61956,-122.48161,0
     * A_shp,37.64430,-122.41070,6
     * A_shp,37.65863,-122.30839,11
     * </pre>
     */
    public long getSequence() {
        return this.sequence;
    }

    /**
     * shape_dist_traveled Optional When used in the shapes.txt file, the shape_dist_traveled field
     * positions a shape point as a distance traveled along a shape from the first shape point. The
     * shape_dist_traveled field represents a real distance traveled along the route in units such
     * as feet or kilometers. This information allows the trip planner to determine how much of the
     * shape to draw when showing part of a trip on the map. The values used for shape_dist_traveled
     * must increase along with shape_pt_sequence: they cannot be used to show reverse travel along
     * a route. The units used for shape_dist_traveled in the shapes.txt file must match the units
     * that are used for this field in the stop_times.txt file. For example, if a bus travels along
     * the three points defined above for A_shp, the additional shape_dist_traveled values (shown
     * here in kilometers) would look like this:
     *
     * <pre>
     * A_shp,37.61956,-122.48161,0,0
     * A_shp,37.64430,-122.41070,6,6.8310
     * A_shp,37.65863,-122.30839,11,15.8765
     * </pre>
     */
    public double getDistanceTraveled() {
        return this.distanceTraveled;
    }

    @Override
    public int compareTo(Shape o) {
        return (int) this.sequence - (int) o.sequence;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).build();
    }

}
