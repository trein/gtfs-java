package com.trein.gtfs.service.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import com.trein.gtfs.mongo.entity.DirectionType;
import com.trein.gtfs.mongo.entity.Route;
import com.trein.gtfs.mongo.entity.Stop;
import com.trein.gtfs.mongo.entity.StopTime;
import com.trein.gtfs.mongo.entity.Trip;
import com.trein.gtfs.mongo.repository.RouteRepository;
import com.trein.gtfs.mongo.repository.StopRepository;
import com.trein.gtfs.mongo.repository.StopTimeRepository;
import com.trein.gtfs.mongo.repository.TripRepository;
import com.trein.gtfs.service.endpoint.v1.GtfsRestServiceImpl;
import com.trein.gtfs.service.endpoint.v1.bean.RouteBean;
import com.trein.gtfs.service.endpoint.v1.bean.ShapeBean;
import com.trein.gtfs.service.endpoint.v1.bean.StopBean;
import com.trein.gtfs.service.endpoint.v1.bean.StopTimeBean;
import com.trein.gtfs.service.endpoint.v1.bean.TripBean;

@Component
public class CachedRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsRestServiceImpl.class);
    private static final Distance DISTANCE = new Distance(0.5, Metrics.KILOMETERS);
    
    @Autowired
    private StopRepository stopRepository;
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private StopTimeRepository stopTimeRepository;
    
    public CachedRepository() {
    }
    
    @Cacheable(value = "trips")
    public List<TripBean> getTrips(Integer page) {
        int size = 50;
        int requestedPage = (page == null) ? -1 : page.intValue();
        int nextPage = (requestedPage > size) ? size : requestedPage;
        List<TripBean> beans = new ArrayList<>();
        Page<Trip> content = null;

        if (nextPage == -1) {
            LOGGER.info("Request all trips");
            long maxSize = this.tripRepository.count();
            content = this.tripRepository.findAll(new PageRequest(0, (int) maxSize));
        } else {
            LOGGER.info("Request page {} for trips", String.valueOf(nextPage));
            content = this.tripRepository.findAll(new PageRequest(nextPage, size));
        }

        for (Trip trip : content.getContent()) {
            beans.add(TripBean.fromTrip(trip));
        }

        return beans;
    }
    
    @Cacheable(value = "trip")
    public TripBean getTrip(String tripId) {
        Trip trip = this.tripRepository.findByTripId(tripId);
        return TripBean.fromTrip(trip);
    }
    
    @Cacheable(value = "trip_stop_times")
    public List<StopTimeBean> getStopTimesForTrip(String tripId) {
        Trip trip = this.tripRepository.findByTripId(tripId);
        List<StopTime> stops = this.stopTimeRepository.findByTrip(trip.getId());
        List<StopTimeBean> beans = new ArrayList<>();

        for (StopTime stopTime : stops) {
            beans.add(StopTimeBean.fromStopTime(stopTime));
        }
        return beans;
    }

    @Cacheable(value = "trip_shapes")
    public List<ShapeBean> getTripShapes(String tripId) {
        Trip trip = this.tripRepository.findByTripId(tripId);
        TripBean bean = TripBean.fromTrip(trip);
        return bean.getShapes();
    }

    @Cacheable(value = "route_trips")
    public List<TripBean> getTripsForRoute(String routeId) {
        Route route = this.routeRepository.findByRouteId(routeId);
        Trip inTrip = this.tripRepository.findOneByRouteAndDirectionType(route.getId(), DirectionType.INBOUND);
        Trip outTrip = this.tripRepository.findOneByRouteAndDirectionType(route.getId(), DirectionType.OUTBOUND);
        return Arrays.asList(TripBean.fromTrip(inTrip), TripBean.fromTrip(outTrip));
    }
    
    @Cacheable(value = "routes")
    public List<RouteBean> getRoutes(Integer page) {
        int size = 50;
        int requestedPage = (page == null) ? -1 : page.intValue();
        int nextPage = (requestedPage > size) ? size : requestedPage;
        List<RouteBean> beans = new ArrayList<>();
        Page<Route> content = null;

        if (nextPage == -1) {
            LOGGER.info("Request all routes");
            long maxSize = this.routeRepository.count();
            content = this.routeRepository.findAll(new PageRequest(0, (int) maxSize));
        } else {
            LOGGER.info("Request page {} for routes", String.valueOf(nextPage));
            content = this.routeRepository.findAll(new PageRequest(nextPage, size));
        }

        for (Route route : content.getContent()) {
            beans.add(RouteBean.fromRoute(route));
        }

        return beans;
    }
    
    @Cacheable(value = "nearby_stops")
    public List<StopBean> getNearbyStops(Point point) {
        GeoResults<Stop> stops = this.stopRepository.findByLocationNear(point, DISTANCE);
        List<StopBean> beans = new ArrayList<>();

        for (GeoResult<Stop> geoStop : stops.getContent()) {
            beans.add(StopBean.fromStop(geoStop.getContent()));
        }
        return beans;
    }
    
}
