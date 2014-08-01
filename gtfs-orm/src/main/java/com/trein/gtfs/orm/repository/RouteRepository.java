package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Route;

public interface RouteRepository extends CrudRepository<Route, Long> {
    
    Route findByRouteId(String routeId);

}
