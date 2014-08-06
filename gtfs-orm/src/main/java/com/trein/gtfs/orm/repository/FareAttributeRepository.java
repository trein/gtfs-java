package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.FareAttribute;

public interface FareAttributeRepository extends CrudRepository<FareAttribute, Long> {

}
