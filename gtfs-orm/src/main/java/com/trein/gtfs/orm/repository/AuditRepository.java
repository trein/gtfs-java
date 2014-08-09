package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.app.Audit;

public interface AuditRepository extends CrudRepository<Audit, Long> {

}
