package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.CalendarDate;

public interface CalendarDateRepository extends JpaRepository<CalendarDate, Long> {

}
