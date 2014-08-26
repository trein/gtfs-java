package com.trein.gtfs.service.endpoint.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;

@Path("/gtfs/v1")
public interface GtfsRestService {

    @GET
    @Path("/trips")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getTrips(@QueryParam("page") Integer page) throws Exception;

    @GET
    @Path("/trips/{trip_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getTrip(@PathParam("trip_id") String tripId) throws Exception;
    
    @GET
    @Path("/trips/{trip_id}/stops")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getTripStopTimes(@PathParam("trip_id") String tripId) throws Exception;
    
    @GET
    @Path("/trips/{trip_id}/shapes")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getTripShapes(@PathParam("trip_id") String tripId) throws Exception;
    
    @GET
    @Path("/routes")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getRoutes(@QueryParam("page") Integer page) throws Exception;

    @GET
    @Path("/routes/{route_id}/trips")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response getRouteTrips(@PathParam("route_id") String routeId) throws Exception;
    
    @GET
    @Path("/nearby/stops")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    // http://localhost:8080/rest/gtfs/v1/nearby/stops?lat=-30.030277&lng=-51.230339
    Response getNearbyStops(@QueryParam("lat") String latitude, @QueryParam("lng") String longitude) throws Exception;

}
