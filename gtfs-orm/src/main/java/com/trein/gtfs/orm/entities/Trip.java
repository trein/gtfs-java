package com.trein.gtfs.orm.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * One or more transit agencies that provide the data in this feed.
 *
 * @author trein
 */
@Entity(name = "trips")
@Table(indexes = { @Index(name = "o_trip_idx", columnList = "o_trip_id") })
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column(name = "o_trip_id", nullable = false)
    private String tripId;

    @ManyToOne
    @JoinColumn(name = "route", nullable = false)
    private Route route;

    @ManyToMany
    @OrderColumn(name = "sequence")
    private List<Shape> shapes;
    
    @Column(name = "o_service_id", nullable = false)
    private String serviceId;

    @Column(name = "headsign")
    private String headsign;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "block_id")
    private int blockId;
    
    @Column(name = "direction_type")
    private DirectionType directionType;

    @Column(name = "wheelchair_type")
    private WheelchairType wheelchairType;

    Trip() {

    }

    public Trip(String tripId, Route route, String serviceId, String headsign, String shortName, DirectionType directionType,
            int blockId, List<Shape> shapes, WheelchairType wheelchairType) {
        this.tripId = tripId;
        this.route = route;
        this.serviceId = serviceId;
        this.headsign = headsign;
        this.shortName = shortName;
        this.directionType = directionType;
        this.blockId = blockId;
        this.shapes = shapes;
        this.wheelchairType = wheelchairType;
    }

    public long getId() {
        return this.id;
    }

    /**
     * trip_id Required The trip_id field contains an ID that identifies a trip. The trip_id is
     * dataset unique.
     *
     * @return current trip's id.
     */
    public String getTripId() {
        return this.tripId;
    }

    /**
     * route_id Required The route_id field contains an ID that uniquely identifies a route. This
     * value is referenced from the routes.txt file.
     *
     * @return current trip's related route.
     */
    public Route getRoute() {
        return this.route;
    }

    /**
     * service_id Required The service_id contains an ID that uniquely identifies a set of dates
     * when service is available for one or more routes. This value is referenced from the
     * calendar.txt or calendar_dates.txt file.
     *
     * @return current trip's service id.
     */
    public String getServiceId() {
        return this.serviceId;
    }

    /**
     * trip_headsign Optional The trip_headsign field contains the text that appears on a sign that
     * identifies the trip's destination to passengers. Use this field to distinguish between
     * different patterns of service in the same route. If the headsign changes during a trip, you
     * can override the trip_headsign by specifying values for the the stop_headsign field in
     * stop_times.txt. See a Google Maps screenshot highlighting the headsign.
     *
     * @return current trip's headsign.
     */
    public String getHeadsign() {
        return this.headsign;
    }

    /**
     * trip_short_name Optional The trip_short_name field contains the text that appears in
     * schedules and sign boards to identify the trip to passengers, for example, to identify train
     * numbers for commuter rail trips. If riders do not commonly rely on trip names, please leave
     * this field blank. A trip_short_name value, if provided, should uniquely identify a trip
     * within a service day; it should not be used for destination names or limited/express
     * designations.
     *
     * @return current trip's short name.
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * direction_id Optional The direction_id field contains a binary value that indicates the
     * direction of travel for a trip. Use this field to distinguish between bi-directional trips
     * with the same route_id. This field is not used in routing; it provides a way to separate
     * trips by direction when publishing time tables. You can specify names for each direction with
     * the trip_headsign field.<br>
     * <br>
     * For example, you could use the trip_headsign and direction_id fields together to assign a
     * name to travel in each direction for a set of trips. A trips.txt file could contain these
     * rows for use in time tables:
     *
     * <pre>
     *     trip_id,...,trip_headsign,direction_id
     *     1234,...,to Airport,0
     *     1505,...,to Downtown,1
     * </pre>
     *
     * @return current trip's direction.
     * @see DirectionType refer to documentation for more details.
     */
    public DirectionType getDirectionType() {
        return this.directionType;
    }

    /**
     * block_id Optional The block_id field identifies the block to which the trip belongs. A block
     * consists of two or more sequential trips made using the same vehicle, where a passenger can
     * transfer from one trip to the next just by staying in the vehicle. The block_id must be
     * referenced by two or more trips in trips.txt.
     *
     * @return current trip's block id.
     */
    public int getBlockId() {
        return this.blockId;
    }

    /**
     * shape_id Optional The shape_id field contains an ID that defines a shape for the trip. This
     * value is referenced from the shapes.txt file. The shapes.txt file allows you to define how a
     * line should be drawn on the map to represent a trip.
     *
     * @return shares related to the current trip.
     */
    public List<Shape> getShapes() {
        return this.shapes;
    }

    /**
     * wheelchair_accessible Optional
     *
     * <pre>
     *     0 (or empty) - indicates that there is no accessibility information for the trip
     *     1 - indicates that the vehicle being used on this particular trip can accommodate at least one rider in a wheelchair
     *     2 - indicates that no riders in wheelchairs can be accommodated on this trip
     * </pre>
     *
     * @param type of accessible service available.
     */
    public WheelchairType getWheelchairType() {
        return this.wheelchairType;
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
