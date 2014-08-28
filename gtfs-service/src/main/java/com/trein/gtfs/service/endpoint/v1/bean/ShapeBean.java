package com.trein.gtfs.service.endpoint.v1.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.Shape;

@XmlRootElement
public class ShapeBean {

    @XmlAttribute(name = "shape_id")
    private String shapeId;

    @XmlAttribute(name = "location")
    private double[] location;

    @XmlAttribute(name = "sequence")
    private long sequence;
    
    @XmlAttribute(name = "distance_traveled")
    private double distanceTraveled;
    
    private ShapeBean() {
    }
    
    public String getShapeId() {
        return this.shapeId;
    }
    
    public double[] getLocation() {
        return this.location;
    }
    
    public long getSequence() {
        return this.sequence;
    }
    
    public double getDistanceTraveled() {
        return this.distanceTraveled;
    }
    
    public static ShapeBean fromShape(Shape shape) {
        ShapeBean bean = new ShapeBean();
        bean.shapeId = shape.getShapeId();
        bean.location = shape.getLocation();
        bean.sequence = shape.getSequence();
        bean.distanceTraveled = shape.getDistanceTraveled();
        return bean;
    }
}
