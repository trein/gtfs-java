package com.trein.gtfs.service.aspect.audit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRequest.class);

    private static final String USER_AGENT = "User-Agent";
    private static final String TOKEN = "Token";
    private static final String USER_ID = "User-Id";

    private final HttpServletRequest request;
    private final UriInfo uriInfo;

    public ServiceRequest(HttpServletRequest request, UriInfo uriInfo) {
        this.request = request;
        this.uriInfo = uriInfo;
    }

    public boolean assertValid() {
        boolean valid;

        LOGGER.debug("Checking request token");

        if (getUserAgent().contains("Mozilla")) {
            valid = true;
            LOGGER.warn("Request sent from browser");
        } else if (getToken() == null) {
            valid = false;
            LOGGER.warn("Request with null token");
        } else {
            LOGGER.debug("Token: " + getToken());
            LOGGER.debug("SHA1 requested url: " + getRequestedUriSha1());
            LOGGER.debug("Decoded requested url: " + getRequestUri());
            valid = getRequestedUriSha1().equals(getToken());
        }
        return valid;
    }

    public String getRequestUri() {
        String baseUri = this.uriInfo.getBaseUri().toString() + "transport/";
        String encodedUri = this.uriInfo.getRequestUri().toString().replace(baseUri, "");
        return encodedUri;
    }

    public String getUserAgent() {
        return this.request.getHeader(USER_AGENT);
    }

    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    public String getRemotePort() {
        return String.valueOf(this.request.getRemotePort());
    }

    public String getToken() {
        return this.request.getHeader(TOKEN);
    }
    
    public String getUserId() {
        return this.request.getHeader(USER_ID);
    }
    
    private String getRequestedUriSha1() {
        return DigestUtils.sha1Hex(getRequestUri());
    }
    
    @Override
    public String toString() {
        return getRequestUri();
    }
}
