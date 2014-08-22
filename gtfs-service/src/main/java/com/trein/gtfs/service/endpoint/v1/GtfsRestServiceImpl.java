package com.trein.gtfs.service.endpoint.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import com.trein.gtfs.mongo.entities.Route;
import com.trein.gtfs.mongo.entities.Stop;
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
    public Response getRoutes(Integer page) throws ResourceNotFoundException {
        List<Route> routes = this.repository.getRoutes(page);
        return Response.status(Status.OK).entity(routes).build();
    }
    
    @Override
    public Response getRouteTrips(String routeId) throws ResourceNotFoundException {
        List<Trip> trips = this.repository.getTripsForRoute(routeId);
        LOGGER.info("Found {} trips for route {}", trips.size(), routeId);
        return Response.status(Status.OK).entity(trips).build();
    }

    @Override
    public Response getNearbyStops(String latitude, String longitude) throws ResourceNotFoundException {
        Point point = new Point(Double.parseDouble(latitude), Double.parseDouble(longitude));
        GeoResults<Stop> stops = this.repository.getNearbyStops(point);
        return Response.status(Status.OK).entity(stops).build();
    }

    @Override
    public String getName() {
        return "GTFS Rest Service";
    }
}
