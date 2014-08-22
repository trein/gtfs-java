package com.trein.gtfs.service.runner;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpRequestFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpResponseFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.plugins.server.servlet.HttpServletResponseWrapper;
import org.jboss.resteasy.plugins.server.servlet.ServletBootstrap;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyUriInfo;

public class MultipleMappingsHttpServletDispatcher extends HttpServlet implements HttpRequestFactory, HttpResponseFactory {
    
    private static final long serialVersionUID = 9136920307681826932L;
    protected MultipleServletContainerDispatcher servletContainerDispatcher;
    
    public Dispatcher getDispatcher() {
        return this.servletContainerDispatcher.getDispatcher();
    }
    
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.servletContainerDispatcher = new MultipleServletContainerDispatcher();
        ServletBootstrap bootstrap = new ServletBootstrap(servletConfig);
        this.servletContainerDispatcher.init(servletConfig.getServletContext(), bootstrap, this, this);
        this.servletContainerDispatcher.getDispatcher().getDefaultContextObjects().put(ServletConfig.class, servletConfig);
        
    }
    
    @Override
    public void destroy() {
        super.destroy();
        this.servletContainerDispatcher.destroy();
    }
    
    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        service(httpServletRequest.getMethod(), httpServletRequest, httpServletResponse);
    }
    
    public void service(String httpMethod, HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.servletContainerDispatcher.service(httpMethod, request, response, true);
    }
    
    @Override
    public HttpRequest createResteasyHttpRequest(String httpMethod, HttpServletRequest request, ResteasyHttpHeaders headers,
            ResteasyUriInfo uriInfo, HttpResponse theResponse, HttpServletResponse response) {
        return createHttpRequest(httpMethod, request, headers, uriInfo, theResponse, response);
    }
    
    @Override
    public HttpResponse createResteasyHttpResponse(HttpServletResponse response) {
        return createServletResponse(response);
    }
    
    protected HttpRequest createHttpRequest(String httpMethod, HttpServletRequest request, ResteasyHttpHeaders headers,
            ResteasyUriInfo uriInfo, HttpResponse theResponse, HttpServletResponse response) {
        return new HttpServletInputMessage(request, response, getServletContext(), theResponse, headers, uriInfo, httpMethod
                .toUpperCase(), (SynchronousDispatcher) getDispatcher());
    }
    
    protected HttpResponse createServletResponse(HttpServletResponse response) {
        return new HttpServletResponseWrapper(response, getDispatcher().getProviderFactory());
    }
    
}
