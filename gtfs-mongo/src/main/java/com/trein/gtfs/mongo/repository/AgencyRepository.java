package com.trein.gtfs.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entity.Agency;

public interface AgencyRepository extends MongoRepository<Agency, ObjectId> {

    Agency findByAgencyId(String id);
}
