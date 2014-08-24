package com.trein.gtfs.mongo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.format.support.DefaultFormattingConversionService;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.trein.gtfs.mongo.converters.DateTimeConverter;
import com.trein.gtfs.mongo.datasource.MongoPropertiesResolver;
import com.trein.gtfs.mongo.entities.StopTime;
import com.trein.gtfs.mongo.entities.Trip;

@Configuration
@EnableAspectJAutoProxy
@EnableMongoRepositories
@ComponentScan(basePackages = { "com.trein.gtfs.mongo" })
public class MongoRepositoryConfig {
    
    // @Bean
    // public MongoSetup mongoSetup() throws Exception {
    // return new MongoSetup(mongoTemplate());
    // }

    @Bean
    public MongoPropertiesResolver mongoResolver() {
        return new MongoPropertiesResolver();
    }
    
    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new DateTimeConverter());
        return conversionService;
    }
    
    @Bean
    public Mongo mongo() throws Exception {
        MongoPropertiesResolver resolver = mongoResolver();
        return new MongoClient(resolver.getUrl());
    }
    
    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
        MongoPropertiesResolver resolver = mongoResolver();
        return new SimpleMongoDbFactory(mongo(), resolver.getDbName());
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(), mongoConverter());
        mongoTemplate.indexOps(StopTime.class).ensureIndex(new Index().on("trip", Direction.ASC));
        mongoTemplate.indexOps(Trip.class).ensureIndex(new Index().on("route", Direction.ASC));
        return mongoTemplate;
    }
    
    @Bean
    public CustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
        converters.add(new DateTimeConverter());
        return new CustomConversions(converters);
    }
    
    @Bean
    public MappingMongoConverter mongoConverter() throws Exception {
        MongoMappingContext mappingContext = new MongoMappingContext();
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory());
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
        mongoConverter.setCustomConversions(customConversions());
        return mongoConverter;
    }

}
