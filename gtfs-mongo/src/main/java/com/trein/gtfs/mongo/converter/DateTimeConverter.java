package com.trein.gtfs.mongo.converter;

import java.sql.Time;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class DateTimeConverter implements Converter<Date, Time> {

    @Override
    public Time convert(Date source) {
        return new Time(source.getTime());
    }
    
}
