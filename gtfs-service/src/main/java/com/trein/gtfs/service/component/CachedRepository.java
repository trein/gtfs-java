package com.trein.gtfs.service.component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import com.trein.gtfs.mongo.entities.Route;
import com.trein.gtfs.mongo.entities.Stop;
import com.trein.gtfs.mongo.entities.Trip;
import com.trein.gtfs.mongo.repository.RouteRepository;
import com.trein.gtfs.mongo.repository.StopRepository;
import com.trein.gtfs.mongo.repository.TripRepository;
import com.trein.gtfs.service.endpoint.v1.GtfsRestServiceImpl;

@Component
public class CachedRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsRestServiceImpl.class);
    private static final Distance DISTANCE = new Distance(1, Metrics.KILOMETERS);
    
    @Autowired
    private StopRepository stopRepository;
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private RouteRepository routeRepository;
    
    public CachedRepository() {
    }
    
    @Cacheable(value = "trips")
    public List<Trip> getTrips(Integer page) {
        int size = 50;
        int requestedPage = (page == null) ? -1 : page.intValue();
        int nextPage = (requestedPage > size) ? size : requestedPage;

        if (nextPage == -1) {
            LOGGER.info("Request all trips");
            long maxSize = this.tripRepository.count();
            return this.tripRepository.findAll(new PageRequest(0, (int) maxSize)).getContent();
        }
        LOGGER.info("Request page {} for trips", String.valueOf(nextPage));
        return this.tripRepository.findAll(new PageRequest(nextPage, size)).getContent();
    }
    
    @Cacheable(value = "trip")
    public Trip getTrip(String tripId) {
        return this.tripRepository.findByTripId(tripId);
    }

    @Cacheable(value = "route_trips")
    public List<Trip> getTripsForRoute(String routeId) {
        Route route = this.routeRepository.findByRouteId(routeId);
        Trip trips = this.tripRepository.findOneByRouteId(route.getId());
        return Arrays.asList(trips);
    }
    
    @Cacheable(value = "routes")
    public List<Route> getRoutes(Integer page) {
        int size = 50;
        int requestedPage = (page == null) ? -1 : page.intValue();
        int nextPage = (requestedPage > size) ? size : requestedPage;

        if (nextPage == -1) {
            LOGGER.info("Request all routes");
            long maxSize = this.routeRepository.count();
            return this.routeRepository.findAll(new PageRequest(0, (int) maxSize)).getContent();
        }
        LOGGER.info("Request page {} for routes", String.valueOf(nextPage));
        return this.routeRepository.findAll(new PageRequest(nextPage, size)).getContent();
    }
    
    @Cacheable(value = "nearby_stops")
    public GeoResults<Stop> getNearbyStops(Point point) {
        GeoResults<Stop> stops = this.stopRepository.findByLocationNear(point, DISTANCE);
        return stops;
    }
    
}
