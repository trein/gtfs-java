package com.trein.gtfs.etl.writer.legacy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.orm.entities.Agency;
import com.trein.gtfs.orm.repository.AgencyRepository;

@Component
public class JpaAgencyItemWriter implements ItemWriter<GtfsAgency> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaAgencyItemWriter.class);
    
    private static final boolean DRY_RUN = false;
    
    private final AgencyRepository repository;

    @Autowired
    public JpaAgencyItemWriter(AgencyRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void write(List<? extends GtfsAgency> items) {
        for (GtfsAgency agency : items) {
            LOGGER.debug(agency.toString());
            this.repository.save(new Agency());
        }
    }
    
}
