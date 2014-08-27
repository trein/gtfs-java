package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.StopTime;

public interface StopTimeRepository extends JpaRepository<StopTime, Long> {

}
