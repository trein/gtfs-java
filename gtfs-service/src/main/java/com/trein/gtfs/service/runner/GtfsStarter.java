package com.trein.gtfs.service.runner;

public class GtfsStarter {
    
    public static final String CONTEXT_PATH = "/"; //$NON-NLS-1$
    public static final String CONTEXT_DIR = "src/main/webapp"; //$NON-NLS-1$
    public static final String BASE_DIR = "."; //$NON-NLS-1$
    public static int PORT = 8080;
    
    public static void main(String[] args) throws Exception {
	TomcatEmbeddedServer server = new TomcatEmbeddedServer();
	
	server.setPort(PORT);
	server.setBaseDir(BASE_DIR);
	server.addContext(CONTEXT_PATH, CONTEXT_DIR);
	server.setup();
	server.start();
	server.await();
    }
}
