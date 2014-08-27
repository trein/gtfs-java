package com.trein.gtfs.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entity.Frequency;

public interface FrequencyRepository extends MongoRepository<Frequency, ObjectId> {
    
}
