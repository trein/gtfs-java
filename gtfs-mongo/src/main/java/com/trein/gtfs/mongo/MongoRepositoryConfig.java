package com.trein.gtfs.mongo;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

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
public class MongoRepositoryConfig extends AbstractMongoConfiguration {
    
    @PostConstruct
    public void initDb() throws Exception {
        MongoTemplate mongoTemplate = mongoTemplate();
        mongoTemplate.indexOps(StopTime.class).ensureIndex(new Index().on("trip", Direction.ASC));
        mongoTemplate.indexOps(Trip.class).ensureIndex(new Index().on("route", Direction.ASC));
    }
    
    @Bean
    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(new DateTimeConverter()));
    }

    @Bean
    public MongoPropertiesResolver mongoResolver() {
        return new MongoPropertiesResolver();
    }

    @Bean
    @Override
    public Mongo mongo() throws Exception {
        MongoPropertiesResolver resolver = mongoResolver();
        return new MongoClient(resolver.getUrl());
    }

    @Override
    protected String getDatabaseName() {
        MongoPropertiesResolver resolver = mongoResolver();
        return resolver.getDbName();
    }
    
}
