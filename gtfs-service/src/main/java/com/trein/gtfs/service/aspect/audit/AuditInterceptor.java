package com.trein.gtfs.service.aspect.audit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trein.gtfs.mongo.entities.app.Audit;
import com.trein.gtfs.mongo.repository.AuditRepository;
import com.trein.gtfs.service.endpoint.RestRequestAware;
import com.trein.gtfs.service.exception.SecurityException;

@Aspect
@Component
public class AuditInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditInterceptor.class);

    private final boolean skipAuthentication = false;
    private final boolean enbaleAudit = true;

    @Autowired
    private AuditRepository auditRepository;

    @Around("execution(* (@com.trein.gtfs.service.aspect.audit.ServiceEndpoint *).*(..))")
    public Object doBasicAudit(ProceedingJoinPoint pjp) throws Throwable {
        Object bean = pjp.getTarget();
        Object response = null;
        
        if (bean instanceof RestRequestAware) {
            RestRequestAware service = (RestRequestAware) bean;
            HttpServletRequest request = service.getRequest();
            UriInfo uriInfo = service.getUriInfo();
            ServiceRequest securedRequest = new ServiceRequest(request, uriInfo);
            
            if (this.skipAuthentication || securedRequest.assertValid()) {
                response = auditAndProceed(pjp, service, request, securedRequest);
            } else {
                response = auditAndBlock(service, request, securedRequest);
            }
        }
        return response;
    }

    private Object auditAndBlock(RestRequestAware service, HttpServletRequest request, ServiceRequest securedRequest)
            throws SecurityException {
        
        String pattern = "Non authorized request received for [%s] with request [%s]";
        String message = String.format(pattern, service, request);
        
        LOGGER.warn(message);
        
        if (this.enbaleAudit) {
            auditInsecuredRequest(securedRequest);
        }
        
        throw new SecurityException("Please, do not try to steal our data. Collaborate with us instead.");
    }

    private Object auditAndProceed(ProceedingJoinPoint pjp, RestRequestAware service, HttpServletRequest request,
            ServiceRequest securedRequest) throws Throwable {
        String pattern = "Auditing target [%s] with request [%s]";
        String message = String.format(pattern, service.getName(), securedRequest.toString());
        
        LOGGER.info(message);
        
        if (this.enbaleAudit) {
            auditSecuredRequest(securedRequest);
        }
        return pjp.proceed();
    }

    private void auditSecuredRequest(ServiceRequest securedRequest) {
        auditRequest(securedRequest, "Request received and secured");
    }

    private void auditInsecuredRequest(ServiceRequest insecuredRequest) {
        auditRequest(insecuredRequest, "Insecured request received");
    }

    private void auditRequest(ServiceRequest request, String message) {
        Audit audit = new Audit();
        
        audit.setUserId(request.getUserId());
        audit.setUserAgent(request.getUserAgent());
        audit.setRequestedUri(request.getRequestUri());
        audit.setToken(request.getToken());
        audit.setComment(message);
        
        this.auditRepository.save(audit);
    }

}
