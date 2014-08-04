package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.Transfer;

public interface TransferRepository extends CrudRepository<Transfer, Long> {
    
}
