package com.trein.gtfs.mongo.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.trein.gtfs.mongo.entity.Shape;

public interface ShapeRepository extends MongoRepository<Shape, ObjectId> {
    
    List<Shape> findByShapeId(String shapeId);

    GeoResults<Shape> findByLocationNear(Point point, Distance distance);
    
}
