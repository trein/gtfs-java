package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.CalendarDate;

public interface CalendarDateRepository extends CrudRepository<CalendarDate, Long> {
    
}
