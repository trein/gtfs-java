package com.trein.gtfs.service.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
@Component
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        String response = "{ \"status\": \"error\", \"message\": \"Invalid request mother fucker\"}";
        String logMessage = String.format("Generic exception [%s] captured: %s", exception.getClass(), exception.getMessage());
        LOGGER.error(logMessage, exception);
        return Response.serverError().entity(response).type(MediaType.APPLICATION_XML).build();
    }
}
