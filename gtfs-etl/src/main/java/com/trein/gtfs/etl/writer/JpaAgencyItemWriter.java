package com.trein.gtfs.etl.writer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.trein.gtfs.vo.GtfsAgency;

@Component
public class JpaAgencyItemWriter implements ItemWriter<GtfsAgency> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaAgencyItemWriter.class);
    
    private static final boolean DRY_RUN = false;
    
    // private final ProductRepository repository;
    
    // @Autowired
    // public JpaProductItemWriter(ProductRepository repository) {
    // this.repository = repository;
    // }
    
    @Override
    public void write(List<? extends GtfsAgency> items) {
	for (GtfsAgency agency : items) {
	}
    }
    
}
