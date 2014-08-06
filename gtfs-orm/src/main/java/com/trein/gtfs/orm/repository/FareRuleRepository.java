package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.FareRule;

public interface FareRuleRepository extends CrudRepository<FareRule, Long> {
    
}
