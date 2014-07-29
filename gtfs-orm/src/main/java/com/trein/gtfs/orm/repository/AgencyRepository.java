package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Agency;

public interface AgencyRepository extends CrudRepository<Agency, Long> {

}
