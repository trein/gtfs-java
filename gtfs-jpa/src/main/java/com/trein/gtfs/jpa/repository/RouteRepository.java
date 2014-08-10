package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Route findByRouteId(String routeId);
    
}
