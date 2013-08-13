package com.trein.gtfs.etl.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.trein.gtfs.vo.GtfsAgency;

public class AgencyFieldSetMapper implements FieldSetMapper<GtfsAgency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgencyFieldSetMapper.class);

    private static final int FIELDS_COUNT = 3;

    private static final int DESC_POS = 2;
    private static final int CODE_POS = 1;

    public AgencyFieldSetMapper() {
    }

    @Override
    public GtfsAgency mapFieldSet(FieldSet fieldSet) throws BindException {
        GtfsAgency agency = new GtfsAgency();

        LOGGER.debug(fieldSet.toString());

        return agency;
    }

}
