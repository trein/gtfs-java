package com.trein.gtfs.service.runner;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for embedded Tomcat server.
 */
public class TomcatEmbeddedServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatEmbeddedServer.class);
    
    public static final String DEFAULT_BASE_DIR = "."; //$NON-NLS-1$
    public static int DEFAULT_PORT = 8080;
    
    private Tomcat tomcat;
    private int port = DEFAULT_PORT;
    private String baseDir = DEFAULT_BASE_DIR;
    
    public TomcatEmbeddedServer() {
	LOGGER.debug("Creating a new instance of Tomcat Embedded Server."); //$NON-NLS-1$
	createServer();
    }
    
    public void setup() throws Exception {
	LOGGER.debug("Setting up server."); //$NON-NLS-1$
	setupServer();
	addListener();
    }
    
    public void start() throws Exception {
	LOGGER.debug("Starting server."); //$NON-NLS-1$
	this.tomcat.start();
    }
    
    public void await() {
	this.tomcat.getServer().await();
    }
    
    private void createServer() {
	this.tomcat = new Tomcat();
    }
    
    private void setupServer() throws Exception {
	this.tomcat.setPort(this.port);
	this.tomcat.setBaseDir(this.baseDir);
    }
    
    private void addListener() {
	StandardServer server = (StandardServer) this.tomcat.getServer();
	AprLifecycleListener listener = new AprLifecycleListener();
	server.addLifecycleListener(listener);
    }
    
    public void addContext(String contextPath, String baseDir) throws Exception {
	LOGGER.debug(String.format("Adding context [%s].", contextPath)); //$NON-NLS-1$
	this.tomcat.addWebapp(contextPath, new File(baseDir).getAbsolutePath());
    }
    
    public void setPort(int port) {
	this.port = port;
    }
    
    public int getPort() {
	return this.port;
    }
    
    public void setBaseDir(String baseDir) {
	this.baseDir = baseDir;
    }
    
    public String getBaseDir() {
	return this.baseDir;
    }
    
    public void terminate() {
	LOGGER.debug("Stoping server."); //$NON-NLS-1$
	try {
	    this.tomcat.stop();
	} catch (LifecycleException e) {
	    e.printStackTrace();
	}
    }
    
}
