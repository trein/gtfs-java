package com.trein.gtfs.orm.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Shape;

public interface ShapeRepository extends CrudRepository<Shape, Long> {
    
    List<Shape> findByShapeId(String shapeId);
    
}
