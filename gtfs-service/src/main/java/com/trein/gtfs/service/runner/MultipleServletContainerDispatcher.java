package com.trein.gtfs.service.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.servlet.ConfigurationBootstrap;
import org.jboss.resteasy.plugins.server.servlet.HttpRequestFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpResponseFactory;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ServletSecurityContext;
import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.resteasy.util.GetRestful;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MultipleServletContainerDispatcher {
    protected Dispatcher dispatcher;
    protected ResteasyProviderFactory providerFactory;
    private final static Logger logger = Logger.getLogger(ServletContainerDispatcher.class);
    private String servletMappingPrefix = "";
    protected ResteasyDeployment deployment = null;
    protected HttpRequestFactory requestFactory;
    protected HttpResponseFactory responseFactory;
    
    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }
    
    public void init(ServletContext servletContext, ConfigurationBootstrap bootstrap, HttpRequestFactory requestFactory,
            HttpResponseFactory responseFactory) throws ServletException {
        this.requestFactory = requestFactory;
        this.responseFactory = responseFactory;
        ResteasyProviderFactory globalFactory = (ResteasyProviderFactory) servletContext
                .getAttribute(ResteasyProviderFactory.class.getName());
        Dispatcher globalDispatcher = (Dispatcher) servletContext.getAttribute(Dispatcher.class.getName());
        
        String application = bootstrap.getInitParameter("javax.ws.rs.Application");
        String useGlobalStr = bootstrap.getInitParameter("resteasy.servlet.context.deployment");
        boolean useGlobal = globalFactory != null;
        if (useGlobalStr != null) {
            useGlobal = Boolean.parseBoolean(useGlobalStr);
        }
        
        // use global is backward compatible with 2.3.x and earlier and will store and/or use the
        // dispatcher and provider factory
        // in the servlet context
        if (useGlobal) {
            this.providerFactory = globalFactory;
            this.dispatcher = globalDispatcher;
            if (((this.providerFactory != null) && (this.dispatcher == null))
                    || ((this.providerFactory == null) && (this.dispatcher != null))) { throw new ServletException(
                    "Unknown state.  You have a Listener messing up what resteasy expects"); }
            // We haven't been initialized by an external entity so bootstrap ourselves
            if (this.providerFactory == null) {
                this.deployment = bootstrap.createDeployment();
                this.deployment.start();
                
                servletContext.setAttribute(ResteasyProviderFactory.class.getName(), this.deployment.getProviderFactory());
                servletContext.setAttribute(Dispatcher.class.getName(), this.deployment.getDispatcher());
                servletContext.setAttribute(Registry.class.getName(), this.deployment.getRegistry());
                
                this.dispatcher = this.deployment.getDispatcher();
                this.providerFactory = this.deployment.getProviderFactory();
                
            } else {
                // ResteasyBootstrap inited us. Check to see if the servlet defines an Application
                // class
                if (application != null) {
                    try {
                        Map contextDataMap = ResteasyProviderFactory.getContextDataMap();
                        contextDataMap.putAll(this.dispatcher.getDefaultContextObjects());
                        Application app = ResteasyDeployment.createApplication(application.trim(), this.dispatcher,
                                this.providerFactory);
                        // push context data so we can inject it
                        processApplication(app);
                    } finally {
                        ResteasyProviderFactory.removeContextDataLevel();
                    }
                }
            }
            this.servletMappingPrefix = bootstrap.getParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX);
            if (this.servletMappingPrefix == null) {
                this.servletMappingPrefix = "";
            }
            this.servletMappingPrefix = this.servletMappingPrefix.trim();
        } else {
            this.deployment = bootstrap.createDeployment();
            this.deployment.start();
            this.dispatcher = this.deployment.getDispatcher();
            this.providerFactory = this.deployment.getProviderFactory();
            
            this.servletMappingPrefix = bootstrap.getParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX);
            if (this.servletMappingPrefix == null) {
                this.servletMappingPrefix = "";
            }
            this.servletMappingPrefix = this.servletMappingPrefix.trim();
        }
    }
    
    public void destroy() {
        if (this.deployment != null) {
            this.deployment.stop();
        }
    }
    
    protected void processApplication(Application config) {
        logger.info("Deploying " + Application.class.getName() + ": " + config.getClass());
        ArrayList<Class> actualResourceClasses = new ArrayList<Class>();
        ArrayList<Class> actualProviderClasses = new ArrayList<Class>();
        ArrayList resources = new ArrayList();
        ArrayList providers = new ArrayList();
        if (config.getClasses() != null) {
            for (Class clazz : config.getClasses()) {
                if (GetRestful.isRootResource(clazz)) {
                    logger.info("Adding class resource " + clazz.getName() + " from Application " + config.getClass());
                    actualResourceClasses.add(clazz);
                } else {
                    logger.info("Adding provider class " + clazz.getName() + " from Application " + config.getClass());
                    actualProviderClasses.add(clazz);
                }
            }
        }
        if (config.getSingletons() != null) {
            for (Object obj : config.getSingletons()) {
                if (GetRestful.isRootResource(obj.getClass())) {
                    logger.info("Adding singleton resource " + obj.getClass().getName() + " from Application "
                            + config.getClass());
                    resources.add(obj);
                } else {
                    logger.info("Adding singleton provider " + obj.getClass().getName() + " from Application "
                            + config.getClass());
                    providers.add(obj);
                }
            }
        }
        for (Class clazz : actualProviderClasses) {
            this.providerFactory.registerProvider(clazz);
        }
        for (Object obj : providers) {
            this.providerFactory.registerProviderInstance(obj);
        }
        for (Class clazz : actualResourceClasses) {
            this.dispatcher.getRegistry().addPerRequestResource(clazz);
        }
        for (Object obj : resources) {
            this.dispatcher.getRegistry().addSingletonResource(obj);
        }
    }
    
    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    public void service(String httpMethod, HttpServletRequest request, HttpServletResponse response, boolean handleNotFound)
            throws IOException, NotFoundException {
        try {
            // logger.info(httpMethod + " " + request.getRequestURL().toString());
            // logger.info("***PATH: " + request.getRequestURL());
            // classloader/deployment aware RestasyProviderFactory. Used to have request specific
            // ResteasyProviderFactory.getInstance()
            ResteasyProviderFactory defaultInstance = ResteasyProviderFactory.getInstance();
            if (defaultInstance instanceof ThreadLocalResteasyProviderFactory) {
                ThreadLocalResteasyProviderFactory.push(this.providerFactory);
            }
            ResteasyHttpHeaders headers = null;
            ResteasyUriInfo uriInfo = null;
            try {
                headers = ServletUtil.extractHttpHeaders(request);
                String[] prefixes = this.servletMappingPrefix.split(",");
                
                for (String prefix : prefixes) {
                    if (request.getRequestURL().toString().contains(String.format("%s/", prefix))) {
                        uriInfo = ServletUtil.extractUriInfo(request, prefix);
                        break;
                    }
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                // made it warn so that people can filter this.
                logger.warn("Failed to parse request.", e);
                return;
            }
            
            HttpResponse theResponse = this.responseFactory.createResteasyHttpResponse(response);
            HttpRequest in = this.requestFactory.createResteasyHttpRequest(httpMethod, request, headers, uriInfo, theResponse,
                    response);
            
            try {
                ResteasyProviderFactory.pushContext(HttpServletRequest.class, request);
                ResteasyProviderFactory.pushContext(HttpServletResponse.class, response);
                
                ResteasyProviderFactory.pushContext(SecurityContext.class, new ServletSecurityContext(request));
                if (handleNotFound) {
                    this.dispatcher.invoke(in, theResponse);
                } else {
                    ((SynchronousDispatcher) this.dispatcher).invokePropagateNotFound(in, theResponse);
                }
            } finally {
                ResteasyProviderFactory.clearContextData();
            }
        } finally {
            ResteasyProviderFactory defaultInstance = ResteasyProviderFactory.getInstance();
            if (defaultInstance instanceof ThreadLocalResteasyProviderFactory) {
                ThreadLocalResteasyProviderFactory.pop();
            }
            
        }
    }
}
