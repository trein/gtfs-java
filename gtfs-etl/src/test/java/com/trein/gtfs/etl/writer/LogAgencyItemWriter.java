package com.trein.gtfs.etl.writer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.trein.gtfs.csv.vo.GtfsAgency;

public class LogAgencyItemWriter implements ItemWriter<GtfsAgency> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAgencyItemWriter.class);
    
    @Override
    public void write(List<? extends GtfsAgency> items) throws Exception {
	LOGGER.debug(items.toString());
    }
    
}
