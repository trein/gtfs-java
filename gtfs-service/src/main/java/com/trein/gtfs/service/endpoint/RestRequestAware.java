package com.trein.gtfs.service.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public interface RestRequestAware {

    HttpServletRequest getRequest();
    
    UriInfo getUriInfo();

    String getName();
}
