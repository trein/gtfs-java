package com.trein.gtfs.orm.repository;

import org.springframework.data.repository.CrudRepository;

import com.trein.gtfs.orm.entities.FeedInfo;

public interface FeedInfoRepository extends CrudRepository<FeedInfo, Long> {
    
}
