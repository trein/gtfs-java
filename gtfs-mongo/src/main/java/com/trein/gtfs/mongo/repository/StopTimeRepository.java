package com.trein.gtfs.mongo.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.trein.gtfs.mongo.entity.StopTime;

public interface StopTimeRepository extends MongoRepository<StopTime, ObjectId> {

    @Query(fields = "{ 'trip' : 0 }")
    List<StopTime> findByTrip(ObjectId tripId);

}
