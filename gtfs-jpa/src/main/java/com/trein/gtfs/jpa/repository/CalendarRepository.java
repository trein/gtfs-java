package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.Calendar;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    
}
