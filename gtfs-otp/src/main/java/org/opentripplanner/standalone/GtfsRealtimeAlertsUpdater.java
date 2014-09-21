package org.opentripplanner.standalone;

import java.io.InputStream;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GTFS-RT alerts updater Usage example ('myalert' name is an example) in file 'Graph.properties':
 *
 * <pre>
 * myalert.type = real-time-alerts
 * myalert.frequencySec = 60
 * myalert.url = http://host.tld/path
 * myalert.earlyStartSec = 3600
 * myalert.defaultAgencyId = TA
 * </pre>
 */
public class GtfsRealtimeAlertsUpdater extends PollingGraphUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(GtfsRealtimeAlertsUpdater.class);
    
    private GraphUpdaterManager updaterManager;
    
    private Long lastTimestamp = Long.MIN_VALUE;
    
    private String url;
    
    private String defaultAgencyId;
    
    private AlertPatchService alertPatchService;
    
    private long earlyStart;
    
    private AlertsUpdateHandler updateHandler = null;
    
    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.updaterManager = updaterManager;
    }
    
    @Override
    protected void configurePolling(Graph graph, Preferences preferences) throws Exception {
        // TODO: add options to choose different patch services
        AlertPatchService alertPatchService = new AlertPatchServiceImpl(graph);
        this.alertPatchService = alertPatchService;
        String url = preferences.get("url", null);
        if (url == null) { throw new IllegalArgumentException("Missing mandatory 'url' parameter"); }
        this.url = url;
        this.earlyStart = preferences.getInt("earlyStartSec", 0);
        this.defaultAgencyId = preferences.get("defaultAgencyId", null);
        LOG.info("Creating real-time alert updater running every {} seconds : {}", this.frequencySec, url);
    }
    
    @Override
    public void setup() {
        if (this.updateHandler == null) {
            this.updateHandler = new AlertsUpdateHandler();
        }
        this.updateHandler.setEarlyStart(this.earlyStart);
        this.updateHandler.setDefaultAgencyId(this.defaultAgencyId);
        this.updateHandler.setAlertPatchService(this.alertPatchService);
    }
    
    @Override
    protected void runPolling() {
        try {
            InputStream data = HttpUtils.getData(this.url);
            if (data == null) { throw new RuntimeException("Failed to get data from url " + this.url); }
            
            final FeedMessage feed = FeedMessage.PARSER.parseFrom(data);
            
            long feedTimestamp = feed.getHeader().getTimestamp();
            if (feedTimestamp <= this.lastTimestamp) {
                LOG.info("Ignoring feed with an old timestamp.");
                return;
            }
            
            // Handle update in graph writer runnable
            this.updaterManager.execute(new GraphWriterRunnable() {
                @Override
                public void run(Graph graph) {
                    GtfsRealtimeAlertsUpdater.this.updateHandler.update(feed);
                }
            });
            
            this.lastTimestamp = feedTimestamp;
        } catch (Exception e) {
            LOG.error("Error reading gtfs-realtime feed from " + this.url, e);
        }
    }
    
    @Override
    public void teardown() {
    }
    
    @Override
    public String toString() {
        return "GtfsRealtimeUpdater(" + this.url + ")";
    }
}
