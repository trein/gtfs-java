package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Trip;

public interface TripRepository extends CrudRepository<Trip, Long> {
    
    Trip findByTripId(String tripId);

}
