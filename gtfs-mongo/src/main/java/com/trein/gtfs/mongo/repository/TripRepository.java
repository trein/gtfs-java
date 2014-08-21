package com.trein.gtfs.mongo.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.trein.gtfs.mongo.entities.Trip;

public interface TripRepository extends MongoRepository<Trip, ObjectId> {

    Trip findByTripId(String tripId);

    @Query(value = "{ 'route' : ?0 }")
    List<Trip> findByRouteId(ObjectId id);
    
    @Query(value = "{ 'route' : ?0 }")
    Trip findOneByRouteId(ObjectId id);
    
}
