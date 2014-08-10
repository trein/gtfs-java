package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.Frequency;

public interface FrequencyRepository extends JpaRepository<Frequency, Long> {
    
}
