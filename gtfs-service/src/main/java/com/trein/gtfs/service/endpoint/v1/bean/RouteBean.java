package com.trein.gtfs.service.endpoint.v1.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.Route;
import com.trein.gtfs.mongo.entity.RouteType;

@XmlRootElement
public class RouteBean {
    
    @XmlAttribute(name = "route_id")
    private String routeId;

    @XmlAttribute(name = "agency")
    private AgencyBean agency;
    
    @XmlAttribute(name = "short_name")
    private String shortName;
    
    @XmlAttribute(name = "long_name")
    private String longName;

    @XmlAttribute(name = "type")
    private RouteType type;

    @XmlAttribute(name = "desc")
    private String desc;

    @XmlAttribute(name = "url")
    private String url;

    @XmlAttribute(name = "hex_path_color")
    private String hexPathColor;

    @XmlAttribute(name = "hex_text_color")
    private String hexTextColor;
    
    private RouteBean() {
    }

    public String getRouteId() {
        return this.routeId;
    }

    public AgencyBean getAgency() {
        return this.agency;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getLongName() {
        return this.longName;
    }

    public RouteType getType() {
        return this.type;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getUrl() {
        return this.url;
    }

    public String getHexPathColor() {
        return this.hexPathColor;
    }

    public String getHexTextColor() {
        return this.hexTextColor;
    }

    public static RouteBean fromRoute(Route route) {
        RouteBean bean = new RouteBean();
        bean.routeId = route.getRouteId();
        bean.agency = AgencyBean.fromAgency(route.getAgency());
        bean.shortName = route.getShortName();
        bean.longName = route.getLongName();
        bean.type = route.getType();
        bean.desc = route.getDesc();
        bean.url = route.getUrl();
        bean.hexPathColor = route.getHexPathColor();
        bean.hexTextColor = route.getHexTextColor();
        return bean;
    }
}
