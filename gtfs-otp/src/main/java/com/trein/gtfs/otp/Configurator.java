package com.trein.gtfs.otp;

import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.opentripplanner.standalone.GraphBuilderTask;
import org.opentripplanner.standalone.OTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class Configurator {

    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

    // FIXME
    private final String graphDirectory = "";
    private final List<String> routerIds = Lists.newArrayList();
    
    private GraphService graphService = null;
    private OTPServer server;
    
    /**
     * We could even do this at Configurator construct time (rather than lazy initializing), using
     * the inMemory param to create the right kind of GraphService ahead of time. However that would
     * create indexes even when only a build was going to happen.
     */
    // public OTPServer getServer() {
    // if (this.server == null) {
    // this.server = new OTPServer(this.params, getGraphService());
    // }
    // return this.server;
    // }

    public void build() {
        GraphBuilderTask graphBuilder = builderFromParameters();
        if (graphBuilder != null) {
            graphBuilder.run();
        }
    }
    
    public void start() {
        GraphService service = getGraphService();
    }
    
    private GraphService makeGraphService() {
        GraphServiceImpl graphService = new GraphServiceImpl();
        if (this.graphDirectory != null) {
            graphService.setPath(this.graphDirectory);
        }
        if (this.routerIds.size() > 0) {
            graphService.setDefaultRouterId(this.routerIds.get(0));
            graphService.autoRegister = this.routerIds;
        }
        graphService.startup();
        return graphService;
    }

    private GraphService getGraphService() {
        if (this.graphService == null) {
            this.graphService = makeGraphService();
        }
        return this.graphService;
    }
    
    private GraphBuilderTask builderFromParameters(List<File> buildFiles) {
        LOG.info("Wiring up and configuring graph builder task.");
        GraphBuilderTask graphBuilder = new GraphBuilderTask();
        List<File> gtfsFiles = Lists.newArrayList();
        List<File> osmFiles = Lists.newArrayList();
        File configFile = null;
        
        /*
         * For now this is adding files from all directories listed, rather than building multiple
         * graphs.
         */
        for (File dir : buildFiles) {
            LOG.info("Searching for graph builder input files in {}", dir);
            
            graphBuilder.setPath(dir);
            
            for (File file : dir.listFiles()) {
                switch (InputFileType.forFile(file)) {
                    case GTFS:
                        LOG.info("Found GTFS file {}", file);
                        gtfsFiles.add(file);
                        break;
                    case OSM:
                        LOG.info("Found OSM file {}", file);
                        osmFiles.add(file);
                        break;
                    case OTHER:
                        LOG.debug("Skipping file '{}'", file);
                        break;
                    default:
                        LOG.debug("Skipping file '{}'", file);
                        break;
                }
            }
        }
        
        boolean hasOSM = !osmFiles.isEmpty();
        boolean hasGTFS = !gtfsFiles.isEmpty();
        
        if (hasOSM) {
            List<OpenStreetMapProvider> osmProviders = Lists.newArrayList();
            for (File osmFile : osmFiles) {
                OpenStreetMapProvider osmProvider = new AnyFileBasedOpenStreetMapProviderImpl(osmFile);
                osmProviders.add(osmProvider);
            }
            OpenStreetMapGraphBuilderImpl osmBuilder = new OpenStreetMapGraphBuilderImpl(osmProviders);
            DefaultWayPropertySetSource defaultWayPropertySetSource = new DefaultWayPropertySetSource();
            osmBuilder.setDefaultWayPropertySetSource(defaultWayPropertySetSource);
            osmBuilder.skipVisibility = this.params.skipVisibility;
            graphBuilder.addGraphBuilder(osmBuilder);
            graphBuilder.addGraphBuilder(new PruneFloatingIslands());
        }
        if (hasGTFS) {
            List<GtfsBundle> gtfsBundles = Lists.newArrayList();
            for (File gtfsFile : gtfsFiles) {
                GtfsBundle gtfsBundle = new GtfsBundle(gtfsFile);
                gtfsBundle.setTransfersTxtDefinesStationPaths(this.params.useTransfersTxt);
                if (!this.params.noParentStopLinking) {
                    gtfsBundle.linkStopsToParentStations = true;
                }
                gtfsBundle.parentStationTransfers = this.params.parentStationTransfers;
                gtfsBundles.add(gtfsBundle);
            }
            GtfsGraphBuilderImpl gtfsBuilder = new GtfsGraphBuilderImpl(gtfsBundles);
            graphBuilder.addGraphBuilder(gtfsBuilder);
            // When using the long distance path service, or when there is no street data,
            // link stops to each other based on distance only, unless user has requested linking
            // based on transfers.txt or the street network (if available).
            if ((!hasOSM) || this.params.longDistance) {
                if (!this.params.useTransfersTxt) {
                    if (!hasOSM || !this.params.useStreetsForLinking) {
                        graphBuilder.addGraphBuilder(new StreetlessStopLinker());
                    }
                }
            }
            if (hasOSM) {
                graphBuilder.addGraphBuilder(new TransitToTaggedStopsGraphBuilderImpl());
                graphBuilder.addGraphBuilder(new TransitToStreetNetworkGraphBuilderImpl());
                // The stops can be linked to each other once they have links to the street network.
                if (this.params.longDistance && this.params.useStreetsForLinking && !this.params.useTransfersTxt) {
                    graphBuilder.addGraphBuilder(new StreetfulStopLinker());
                }
            }
            gtfsBuilder.setFareServiceFactory(new DefaultFareServiceFactory());
        }
        graphBuilder.serializeGraph = true;
        return graphBuilder;
    }

    public GrizzlyServer serverFromParameters() {
        if (this.params.server) {
            GrizzlyServer server = new GrizzlyServer(this.params, getServer());
            return server;
        } else {
            return null;
        }
    }
    
    public GraphVisualizer visualizerFromParameters() {
        if (this.params.visualize) {
            // FIXME get OTPServer into visualizer.
            getServer();
            GraphVisualizer visualizer = new GraphVisualizer(getGraphService());
            return visualizer;
        } else {
            return null;
        }
    }

    private static enum InputFileType {
        GTFS, OSM, CONFIG, OTHER;
        public static InputFileType forFile(File file) {
            String name = file.getName();
            if (name.endsWith(".zip")) {
                try {
                    ZipFile zip = new ZipFile(file);
                    ZipEntry stopTimesEntry = zip.getEntry("stop_times.txt");
                    zip.close();
                    if (stopTimesEntry != null) { return GTFS; }
                } catch (Exception e) { /* fall through */
                }
            }
            if (name.endsWith(".pbf")) { return OSM; }
            if (name.endsWith(".osm")) { return OSM; }
            if (name.endsWith(".osm.xml")) { return OSM; }
            if (name.equals("Embed.properties")) { return CONFIG; }
            return OTHER;
        }
    }

}
