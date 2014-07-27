package com.trein.gtfs.etl.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

public class GtfsItemWriter implements ItemWriter<GtfsItem> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsItemWriter.class);
    private static final boolean DRY_RUN = false;
    
    public GtfsItemWriter() {
    }
    
    @Override
    public void write(List<? extends GtfsItem> items) {
        
    }
    
}
