package com.trein.gtfs.service.aspect.validation;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.trein.gtfs.service.endpoint.RestRequestAware;

@Aspect
@Component
public class ValidationInterceptor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationInterceptor.class);
    
    @Around("@annotation(validableRequest)")
    private Object validateRequest(ProceedingJoinPoint pjp, ValidableRequest validableRequest) throws Throwable {
	Response response = null;
	
	if (pjp.getThis() instanceof RestRequestAware) {
	    RestRequestAware service = (RestRequestAware) pjp.getThis();
	    HttpServletRequest request = service.getRequest();
	    
	    LOGGER.info(String.format("Validating target [%s] with request [%s]", service, request));
	    
	    return evaluateRequest(pjp, service);
	}
	return response;
    }
    
    private Object evaluateRequest(ProceedingJoinPoint pjp, RestRequestAware service) throws Throwable {
	validateParams(service.getRequest(), pjp.getArgs());
	
	return pjp.proceed();
    }
    
    private static void validateParams(HttpServletRequest request, Object... params) {
	String error = String.format("Invalid request received in query [%s] and params [%s].", request, Arrays.toString(params));
	
	if (params == null) {
	    LOGGER.error(error);
	    throw new IllegalArgumentException("Invalid request."); //$NON-NLS-1$
	}
	if (params.length == 0) {
	    LOGGER.error(error);
	    throw new IllegalArgumentException("Invalid request."); //$NON-NLS-1$
	}
	for (Object param : params) {
	    if (param == null) {
		LOGGER.error(error);
		throw new IllegalArgumentException("Invalid request."); //$NON-NLS-1$
	    }
	    if ((param instanceof String) && ((String) param).equalsIgnoreCase("null")) { //$NON-NLS-1$
		LOGGER.error(error);
		throw new IllegalArgumentException("Invalid request."); //$NON-NLS-1$
	    }
	}
    }
    
}
