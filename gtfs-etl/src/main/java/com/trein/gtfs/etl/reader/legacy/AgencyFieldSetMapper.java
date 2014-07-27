package com.trein.gtfs.etl.reader.legacy;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.csv.vo.GtfsRoute;

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

        GtfsReaderProvider provider = new GtfsReaderProvider();
        
        GtfsReader<GtfsAgency> gtfsReader = provider.get(GtfsAgency.class);
        GtfsReader<GtfsRoute> gtfssReader = provider.get(GtfsRoute.class);
        
        try {
            List<GtfsAgency> read = gtfsReader.read("br_poa/");
            List<GtfsRoute> r = gtfssReader.read("br_poa/");
            LOGGER.debug(read.toString());
            LOGGER.debug(r.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOGGER.debug(fieldSet.toString());

        return agency;
    }

}
