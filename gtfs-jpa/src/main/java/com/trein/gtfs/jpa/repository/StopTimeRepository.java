package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.StopTime;

public interface StopTimeRepository extends JpaRepository<StopTime, Long> {

}
