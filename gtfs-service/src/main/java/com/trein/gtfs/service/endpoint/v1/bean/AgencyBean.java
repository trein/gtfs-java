package com.trein.gtfs.service.endpoint.v1.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.trein.gtfs.mongo.entity.Agency;

@XmlRootElement
public class AgencyBean {
    
    @XmlAttribute(name = "agency_id")
    private String agencyId;
    
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "url")
    private String url;

    @XmlAttribute(name = "timezone")
    private String timezone;

    @XmlAttribute(name = "lang")
    private String lang;

    @XmlAttribute(name = "phone")
    private String phone;
    
    @XmlAttribute(name = "fare_url")
    private String fareUrl;
    
    private AgencyBean() {
    }
    
    public String getAgencyId() {
        return this.agencyId;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getTimezone() {
        return this.timezone;
    }
    
    public String getLang() {
        return this.lang;
    }
    
    public String getPhone() {
        return this.phone;
    }
    
    public String getFareUrl() {
        return this.fareUrl;
    }
    
    public static AgencyBean fromAgency(Agency agency) {
        AgencyBean bean = new AgencyBean();
        bean.agencyId = agency.getAgencyId();
        bean.name = agency.getName();
        bean.url = agency.getUrl();
        bean.timezone = agency.getTimezone();
        bean.lang = agency.getLang();
        bean.phone = agency.getPhone();
        bean.fareUrl = agency.getFareUrl();
        return bean;
    }
}
