package com.trein.gtfs.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.Shape;

public interface ShapeRepository extends JpaRepository<Shape, Long> {

    List<Shape> findByShapeId(String shapeId);

}
