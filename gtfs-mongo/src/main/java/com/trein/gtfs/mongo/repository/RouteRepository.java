package com.trein.gtfs.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entity.Route;

public interface RouteRepository extends MongoRepository<Route, ObjectId> {

    Route findByRouteId(String routeId);
    
}
