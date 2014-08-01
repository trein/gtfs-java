package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Calendar;

public interface CalendarRepository extends CrudRepository<Calendar, Long> {

}
