package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.FareAttribute;

public interface FareAttributeRepository extends JpaRepository<FareAttribute, Long> {
    
}
