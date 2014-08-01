package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Shape;

public interface ShapeRepository extends CrudRepository<Shape, Long> {
    
    Shape findByShapeId(String shapeId);
    
}
