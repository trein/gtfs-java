package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.StopTime;

public interface StopTimeRepository extends CrudRepository<StopTime, Long> {

    StopTime findByStopTimeId(String stopTime);

}
