package com.trein.gtfs.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entity.FareRule;

public interface FareRuleRepository extends MongoRepository<FareRule, ObjectId> {

}
