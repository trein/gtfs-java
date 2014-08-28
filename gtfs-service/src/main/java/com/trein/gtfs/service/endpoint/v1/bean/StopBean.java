package com.trein.gtfs.service.endpoint.v1.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.Stop;
import com.trein.gtfs.mongo.entity.StopLocationType;
import com.trein.gtfs.mongo.entity.WheelchairType;

@XmlRootElement
public class StopBean {

    @XmlAttribute(name = "stop_id")
    private String stopId;
    
    @XmlAttribute(name = "location")
    private double[] location;

    @XmlAttribute(name = "name")
    private String name;
    
    @XmlAttribute(name = "code")
    private String code;
    
    @XmlAttribute(name = "desc")
    private String desc;
    
    @XmlAttribute(name = "zone")
    private String zone;
    
    @XmlAttribute(name = "url")
    private String url;
    
    @XmlAttribute(name = "timezone")
    private String timezone;
    
    @XmlAttribute(name = "parent_station")
    private int parentStation;
    
    @XmlAttribute(name = "wheelchair_type")
    private WheelchairType wheelchairType;
    
    @XmlAttribute(name = "location_type")
    private StopLocationType locationType;

    private StopBean() {
    }

    public static StopBean fromStop(Stop stop) {
        StopBean bean = new StopBean();
        bean.stopId = stop.getStopId();
        bean.location = stop.getLocation();
        bean.name = stop.getName();
        bean.code = stop.getCode();
        bean.desc = stop.getDesc();
        bean.zone = stop.getZone();
        bean.url = stop.getUrl();
        bean.timezone = stop.getTimezone();
        bean.parentStation = stop.getParentStation();
        bean.wheelchairType = stop.getWheelchairType();
        bean.locationType = stop.getLocationType();
        return bean;
    }
}
