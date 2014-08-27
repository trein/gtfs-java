package com.trein.gtfs.mongo.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Fare {
    
    @Id
    private ObjectId id;

    private String fareId;

    Fare() {
    }

    public Fare(String fareId) {
        this.fareId = fareId;
    }

    public ObjectId getId() {
        return this.id;
    }

    public String getFareId() {
        return this.fareId;
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
