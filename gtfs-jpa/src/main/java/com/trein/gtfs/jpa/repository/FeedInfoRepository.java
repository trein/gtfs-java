package com.trein.gtfs.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trein.gtfs.jpa.entities.FeedInfo;

public interface FeedInfoRepository extends JpaRepository<FeedInfo, Long> {

}
