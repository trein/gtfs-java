package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entity.FareRule;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

}
