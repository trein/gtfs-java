package com.trein.gtfs.service.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
@Component
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityExceptionMapper.class);
    
    @Override
    public Response toResponse(SecurityException exception) {
        String response = "{ \"status\": \"unauthorized\", \"message\": \"Please, do not try to steal our data. Collaborate with us instead.\"}";

        logException(exception);

        return Response.status(Status.UNAUTHORIZED).entity(response).type(MediaType.APPLICATION_XML).build();
    }
    
    private void logException(SecurityException exception) {
        String exceptionMessage = exception.getMessage();
        String exceptionClass = exception.getClass().getName();
        String logMessage = String.format("Security exception [%s] captured: %s", exceptionClass, exceptionMessage);

        LOGGER.error(logMessage, exception);
    }
    
}
