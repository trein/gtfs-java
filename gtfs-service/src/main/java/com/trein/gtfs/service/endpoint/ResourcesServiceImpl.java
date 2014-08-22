package com.trein.gtfs.service.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trein.gtfs.service.component.StaticResources;

@Service
public class ResourcesServiceImpl implements ResourcesService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesServiceImpl.class);
    
    @Context
    private HttpServletRequest request;

    @Autowired
    private StaticResources resources;
    
    @Override
    public Response ping() {
        LOGGER.debug("Health check received from " + this.request.getRemoteHost()); //$NON-NLS-1$
        return Response.status(Status.OK).entity("Health check: alive").build();
    }
    
    @Override
    public Response terms() {
        return Response.ok(this.resources.getTermsOfUse()).build();
    }
}
