package com.trein.gtfs.etl.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.trein.gtfs.vo.Agency;

public class AgencyFieldSetMapper implements FieldSetMapper<Agency> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AgencyFieldSetMapper.class);
    
    private static final int FIELDS_COUNT = 3;
    
    private static final int DESC_POS = 2;
    private static final int CODE_POS = 1;
    
    public AgencyFieldSetMapper() {
    }
    
    @Override
    public Agency mapFieldSet(FieldSet fieldSet) throws BindException {
	Agency agency = null;
	
	LOGGER.debug(fieldSet.toString());
	// product = ;
	return agency;
    }
    
}
