package com.trein.gtfs.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Embeddable
public class Location {

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;
    
    Location() {
        
    }
    
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * stop_lat Required The stop_lat field contains the latitude of a stop or station. The field
     * value must be a valid WGS 84 latitude.
     *
     * @return latitude coordinate for this location.
     */
    public double getLat() {
        return this.latitude;
    }

    /**
     * stop_lon Required The stop_lon field contains the longitude of a stop or station. The field
     * value must be a valid WGS 84 longitude value from -180 to 180.
     *
     * @return longitude coordinate for this location.
     */
    public double getLng() {
        return this.longitude;
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
