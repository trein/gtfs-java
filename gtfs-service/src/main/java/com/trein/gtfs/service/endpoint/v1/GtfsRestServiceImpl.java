package com.trein.gtfs.service.endpoint.v1;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import com.trein.gtfs.mongo.entities.Route;
import com.trein.gtfs.mongo.entities.Shape;
import com.trein.gtfs.mongo.entities.Stop;
import com.trein.gtfs.mongo.entities.StopTime;
import com.trein.gtfs.mongo.entities.Trip;
import com.trein.gtfs.service.aspect.audit.ServiceEndpoint;
import com.trein.gtfs.service.component.CachedRepository;
import com.trein.gtfs.service.endpoint.RestRequestAware;
import com.trein.gtfs.service.exception.ResourceNotFoundException;

@Service
@ServiceEndpoint
public class GtfsRestServiceImpl implements GtfsRestService, RestRequestAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsRestServiceImpl.class);

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;

    @Autowired
    private CachedRepository repository;

    @Override
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public UriInfo getUriInfo() {
        return this.uriInfo;
    }
    
    @Override
    public Response getTrips(Integer page) throws ResourceNotFoundException {
        List<Trip> trips = this.repository.getTrips(page);
        return Response.status(Status.OK).entity(trips).build();
    }

    @Override
    public Response getTrip(@PathParam("trip_id") String tripId) throws UnsupportedEncodingException {
        String decodedTripId = decodeParam(tripId);
        Trip trip = this.repository.getTrip(decodedTripId);
        List<StopTime> stops = this.repository.getStopTimesForTrip(decodedTripId);
        List<Shape> shapes = this.repository.getTripShapes(decodedTripId);
        return Response.status(Status.OK).entity(Arrays.asList(trip, stops, shapes)).build();
    }
    
    @Override
    public Response getTripStopTimes(String tripId) throws ResourceNotFoundException, UnsupportedEncodingException {
        String decodedTripId = decodeParam(tripId);
        List<StopTime> stops = this.repository.getStopTimesForTrip(decodedTripId);
        LOGGER.info("Found {} stop for trip [{}]", String.valueOf(stops.size()), decodedTripId);
        return Response.status(Status.OK).entity(stops).build();
    }

    @Override
    public Response getTripShapes(String tripId) throws ResourceNotFoundException, UnsupportedEncodingException {
        String decodedTripId = decodeParam(tripId);
        List<Shape> shapes = this.repository.getTripShapes(decodedTripId);
        return Response.status(Status.OK).entity(shapes).build();
    }

    @Override
    public Response getRoutes(Integer page) throws ResourceNotFoundException {
        List<Route> routes = this.repository.getRoutes(page);
        return Response.status(Status.OK).entity(routes).build();
    }
    
    @Override
    public Response getRouteTrips(String routeId) throws ResourceNotFoundException, UnsupportedEncodingException {
        String decodedRouteId = decodeParam(routeId);
        List<Trip> trips = this.repository.getTripsForRoute(decodedRouteId);
        LOGGER.info("Found {} trips for route [{}]", String.valueOf(trips.size()), decodedRouteId);
        return Response.status(Status.OK).entity(trips).build();
    }

    @Override
    public Response getNearbyStops(String latitude, String longitude) throws ResourceNotFoundException {
        Point point = new Point(Double.parseDouble(latitude), Double.parseDouble(longitude));
        List<GeoResult<Stop>> stops = this.repository.getNearbyStops(point);
        LOGGER.info("Found {} stops for location [{} {}]", String.valueOf(stops.size()), latitude, longitude);
        return Response.status(Status.OK).entity(stops).build();
    }

    @Override
    public String getName() {
        return "GTFS Rest Service";
    }

    private String decodeParam(String param) throws UnsupportedEncodingException {
        return URLDecoder.decode(param, "UTF-8");
    }
}
