package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Trip findByTripId(String tripId);
    
}
