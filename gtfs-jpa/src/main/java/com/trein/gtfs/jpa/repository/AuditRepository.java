package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.app.Audit;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    
}
