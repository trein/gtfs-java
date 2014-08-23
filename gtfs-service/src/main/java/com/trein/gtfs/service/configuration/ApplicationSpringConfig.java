package com.trein.gtfs.service.configuration;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.trein.gtfs.mongo.MongoRepositoryConfig;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableCaching
@Import({ MongoRepositoryConfig.class })
@ComponentScan(basePackages = { "com.trein.gtfs.service" })
public class ApplicationSpringConfig {
    
    @Bean
    public ConversionService conversionService() {
        return new DefaultFormattingConversionService();
    }
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        Collection<ConcurrentMapCache> caches = new ArrayList<ConcurrentMapCache>();
        caches.add(new ConcurrentMapCache("default"));
        caches.add(new ConcurrentMapCache("trip"));
        caches.add(new ConcurrentMapCache("trips"));
        caches.add(new ConcurrentMapCache("route_trips"));
        caches.add(new ConcurrentMapCache("route"));
        caches.add(new ConcurrentMapCache("routes"));
        caches.add(new ConcurrentMapCache("nearby_stops"));
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
