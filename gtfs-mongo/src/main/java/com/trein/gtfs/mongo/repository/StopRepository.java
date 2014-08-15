package com.trein.gtfs.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entities.Stop;

public interface StopRepository extends MongoRepository<Stop, ObjectId> {

    Stop findByStopId(String parentStation);
    
    GeoResults<Stop> findByLocationWithin(Circle circle);
    
}
