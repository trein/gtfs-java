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
public class ResourceExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExceptionMapper.class);
    
    @Override
    public Response toResponse(ResourceNotFoundException exception) {
	String response = createResponse();
	
	logException(exception);
	
	return Response.status(Status.NOT_FOUND).entity(response).type(MediaType.APPLICATION_XML).build();
    }
    
    private void logException(ResourceNotFoundException exception) {
	String exceptionMessage = exception.getMessage();
	String exceptionClass = exception.getClass().getName();
	String logMessage = String.format("Resource not found exception [%s] captured: %s", exceptionClass, exceptionMessage);
	
	LOGGER.error(logMessage, exception);
    }
    
    private String createResponse() {
	StringBuilder response = new StringBuilder();
	
	response.append("<response>");
	response.append("<status>ERROR</status>");
	response.append("<message>Not Found</message>");
	response.append("</response>");
	
	return response.toString();
    }
    
}
