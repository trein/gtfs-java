package com.trein.gtfs.service.endpoint.v1.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.DirectionType;
import com.trein.gtfs.mongo.entity.Shape;
import com.trein.gtfs.mongo.entity.Trip;
import com.trein.gtfs.mongo.entity.WheelchairType;

@XmlRootElement
public class TripBean {
    
    @XmlAttribute(name = "trip_id")
    private String tripId;

    @XmlAttribute(name = "route")
    private RouteBean route;
    
    @XmlAttribute(name = "shapes")
    private List<ShapeBean> shapes;
    
    @XmlAttribute(name = "service_id")
    private String serviceId;
    
    @XmlAttribute(name = "headsign")
    private String headsign;
    
    @XmlAttribute(name = "short_name")
    private String shortName;

    @XmlAttribute(name = "block_id")
    private int blockId;

    @XmlAttribute(name = "direction_type")
    private DirectionType directionType;
    
    @XmlAttribute(name = "wheelchair_type")
    private WheelchairType wheelchairType;

    private TripBean() {
    }

    public String getTripId() {
        return this.tripId;
    }

    public RouteBean getRoute() {
        return this.route;
    }

    public List<ShapeBean> getShapes() {
        return this.shapes;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getHeadsign() {
        return this.headsign;
    }

    public String getShortName() {
        return this.shortName;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public DirectionType getDirectionType() {
        return this.directionType;
    }

    public WheelchairType getWheelchairType() {
        return this.wheelchairType;
    }

    public static TripBean fromTrip(Trip trip) {
        TripBean bean = new TripBean();
        bean.tripId = trip.getTripId();
        bean.route = RouteBean.fromRoute(trip.getRoute());
        bean.serviceId = trip.getServiceId();
        bean.headsign = trip.getHeadsign();
        bean.shortName = trip.getShortName();
        bean.blockId = trip.getBlockId();
        bean.directionType = trip.getDirectionType();
        bean.wheelchairType = trip.getWheelchairType();
        List<ShapeBean> shapes = new ArrayList<>();
        for (Shape shape : trip.getShapes()) {
            shapes.add(ShapeBean.fromShape(shape));
        }
        bean.shapes = shapes;
        return bean;
    }

}
