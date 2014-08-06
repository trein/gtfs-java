package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Frequency;

public interface FrequencyRepository extends CrudRepository<Frequency, Long> {

}
