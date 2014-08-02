package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Stop;

public interface StopRepository extends CrudRepository<Stop, Long> {
    
    Stop findByStopId(String parentStation);
    
}
