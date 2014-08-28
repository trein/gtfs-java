package com.trein.gtfs.service.endpoint.v1.bean;

import java.sql.Time;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.AvailabilityType;
import com.trein.gtfs.mongo.entity.StopTime;

@XmlRootElement
public class StopTimeBean {

    @XmlAttribute(name = "stop")
    private StopBean stop;
    
    @XmlAttribute(name = "arrival_time")
    private Time arrivalTime;
    
    @XmlAttribute(name = "departure_time")
    private Time departureTime;
    
    @XmlAttribute(name = "sequence")
    private int stopSequence;
    
    @XmlAttribute(name = "headsign")
    private String stopHeadsign;
    
    @XmlAttribute(name = "pickup_type")
    private AvailabilityType pickupType;
    
    @XmlAttribute(name = "dropoff_type")
    private AvailabilityType dropoffType;

    @XmlAttribute(name = "shape_distance_traveled")
    private double shapeDistanceTraveled;
    
    private StopTimeBean() {
    }
    
    public StopBean getStop() {
        return this.stop;
    }
    
    public Time getArrivalTime() {
        return this.arrivalTime;
    }
    
    public Time getDepartureTime() {
        return this.departureTime;
    }
    
    public int getStopSequence() {
        return this.stopSequence;
    }
    
    public String getStopHeadsign() {
        return this.stopHeadsign;
    }
    
    public AvailabilityType getPickupType() {
        return this.pickupType;
    }
    
    public AvailabilityType getDropoffType() {
        return this.dropoffType;
    }
    
    public double getShapeDistanceTraveled() {
        return this.shapeDistanceTraveled;
    }
    
    public static StopTimeBean fromStopTime(StopTime stopTime) {
        StopTimeBean bean = new StopTimeBean();
        bean.stop = StopBean.fromStop(stopTime.getStop());
        bean.arrivalTime = stopTime.getArrivalTime();
        bean.departureTime = stopTime.getDepartureTime();
        bean.stopSequence = stopTime.getStopSequence();
        bean.stopHeadsign = stopTime.getStopHeadsign();
        bean.pickupType = stopTime.getPickupType();
        bean.dropoffType = stopTime.getDropoffType();
        bean.shapeDistanceTraveled = stopTime.getShapeDistanceTraveled();
        return bean;
    }
}
