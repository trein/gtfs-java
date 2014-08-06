package com.trein.gtfs.orm.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "fares")
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Fare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "o_fare_id", nullable = false)
    private String fareId;

    Fare() {
    }

    public Fare(String fareId) {
        this.fareId = fareId;
    }

    public long getId() {
        return this.id;
    }

    public String getFareId() {
        return this.fareId;
    }

}
