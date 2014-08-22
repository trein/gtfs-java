package com.trein.gtfs.service.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;

@Path("/resources")
public interface ResourcesService {
    
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    Response ping();
    
    @GET
    @Path("/terms")
    @Produces(MediaType.TEXT_PLAIN)
    @GZIP
    Response terms();
    
}
