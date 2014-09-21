package org.opentripplanner.standalone;

import java.text.ParseException;
import java.util.List;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be used to create snapshots of lookup tables of realtime data. This is
 * necessary to provide planning threads a consistent constant view of a graph with realtime data at
 * a specific point in time.
 */
public class TimetableSnapshotSource {
    private static final Logger LOG = LoggerFactory.getLogger(TimetableSnapshotSource.class);
    
    public int logFrequency = 2000;
    
    private int appliedBlockCount = 0;
    
    /**
     * If a timetable snapshot is requested less than this number of milliseconds after the previous
     * snapshot, just return the same one. Throttles the potentially resource-consuming task of
     * duplicating a TripPattern -> Timetable map and indexing the new Timetables.
     */
    public int maxSnapshotFrequency = 1000; // msec
    
    /**
     * The last committed snapshot that was handed off to a routing thread. This snapshot may be
     * given to more than one routing thread if the maximum snapshot frequency is exceeded.
     */
    private TimetableResolver snapshot = null;
    
    /** The working copy of the timetable resolver. Should not be visible to routing threads. */
    private final TimetableResolver buffer = new TimetableResolver();
    
    /** Should expired realtime data be purged from the graph. */
    public boolean purgeExpiredData = true;
    
    protected ServiceDate lastPurgeDate = null;
    
    protected long lastSnapshotTime = -1;
    
    private final TimeZone timeZone;
    
    private final GraphIndex graphIndex;
    
    public TimetableSnapshotSource(Graph graph) {
        this.timeZone = graph.getTimeZone();
        this.graphIndex = graph.index;
    }
    
    /**
     * @return an up-to-date snapshot mapping TripPatterns to Timetables. This snapshot and the
     *         timetable objects it references are guaranteed to never change, so the requesting
     *         thread is provided a consistent view of all TripTimes. The routing thread need only
     *         release its reference to the snapshot to release resources.
     */
    public TimetableResolver getTimetableSnapshot() {
        return getTimetableSnapshot(false);
    }
    
    protected synchronized TimetableResolver getTimetableSnapshot(boolean force) {
        long now = System.currentTimeMillis();
        if (force || ((now - this.lastSnapshotTime) > this.maxSnapshotFrequency)) {
            if (force || this.buffer.isDirty()) {
                LOG.debug("Committing {}", this.buffer.toString());
                this.snapshot = this.buffer.commit(force);
            } else {
                LOG.debug("Buffer was unchanged, keeping old snapshot.");
            }
            this.lastSnapshotTime = System.currentTimeMillis();
        } else {
            LOG.debug("Snapshot frequency exceeded. Reusing snapshot {}", this.snapshot);
        }
        return this.snapshot;
    }
    
    /**
     * Method to apply a trip update list to the most recent version of the timetable snapshot.
     */
    public void applyTripUpdates(List<TripUpdate> updates, String agencyId) {
        if (updates == null) {
            LOG.warn("updates is null");
            return;
        }
        
        LOG.debug("message contains {} trip updates", updates.size());
        int uIndex = 0;
        for (TripUpdate tripUpdate : updates) {
            if (!tripUpdate.hasTrip()) {
                LOG.warn("Missing TripDescriptor in gtfs-rt trip update: \n{}", tripUpdate);
                continue;
            }
            
            ServiceDate serviceDate = new ServiceDate();
            TripDescriptor tripDescriptor = tripUpdate.getTrip();
            
            if (tripDescriptor.hasStartDate()) {
                try {
                    serviceDate = ServiceDate.parseString(tripDescriptor.getStartDate());
                } catch (ParseException e) {
                    LOG.warn("Failed to parse startDate in gtfs-rt trip update: \n{}", tripUpdate);
                    continue;
                }
            }
            
            uIndex += 1;
            LOG.debug("trip update #{} ({} updates) :", uIndex, tripUpdate.getStopTimeUpdateCount());
            LOG.trace("{}", tripUpdate);
            
            boolean applied = false;
            if (tripDescriptor.hasScheduleRelationship()) {
                switch (tripDescriptor.getScheduleRelationship()) {
                    case SCHEDULED:
                        applied = handleScheduledTrip(tripUpdate, agencyId, serviceDate);
                        break;
                    case ADDED:
                        applied = handleAddedTrip(tripUpdate, agencyId, serviceDate);
                        break;
                    case UNSCHEDULED:
                        applied = handleUnscheduledTrip(tripUpdate, agencyId, serviceDate);
                        break;
                    case CANCELED:
                        applied = handleCanceledTrip(tripUpdate, agencyId, serviceDate);
                        break;
                    case REPLACEMENT:
                        applied = handleReplacementTrip(tripUpdate, agencyId, serviceDate);
                        break;
                }
            } else {
                // Default
                applied = handleScheduledTrip(tripUpdate, agencyId, serviceDate);
            }
            
            if (applied) {
                this.appliedBlockCount++;
            } else {
                LOG.warn("Failed to apply TripUpdate:\n{}", tripUpdate);
            }
            
            if ((this.appliedBlockCount % this.logFrequency) == 0) {
                LOG.info("Applied {} trip updates.", this.appliedBlockCount);
            }
        }
        LOG.debug("end of update message");
        
        // Make a snapshot after each message in anticipation of incoming requests
        // Purge data if necessary (and force new snapshot if anything was purged)
        if (this.purgeExpiredData) {
            boolean modified = purgeExpiredData();
            getTimetableSnapshot(modified);
        } else {
            getTimetableSnapshot();
        }
    }
    
    protected boolean handleScheduledTrip(TripUpdate tripUpdate, String agencyId, ServiceDate serviceDate) {
        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        AgencyAndId tripId = new AgencyAndId(agencyId, tripDescriptor.getTripId());
        TripPattern pattern = getPatternForTripId(tripId);
        
        if (pattern == null) {
            LOG.warn("No pattern found for tripId {}, skipping TripUpdate.", tripId);
            return false;
        }
        
        if (tripUpdate.getStopTimeUpdateCount() < 1) {
            LOG.warn("TripUpdate contains no updates, skipping.");
            return false;
        }
        
        // we have a message we actually want to apply
        return this.buffer.update(pattern, tripUpdate, agencyId, this.timeZone, serviceDate);
    }
    
    protected boolean handleAddedTrip(TripUpdate tripUpdate, String agencyId, ServiceDate serviceDate) {
        // TODO: Handle added trip
        LOG.warn("Added trips are currently unsupported. Skipping TripUpdate.");
        return false;
    }
    
    protected boolean handleUnscheduledTrip(TripUpdate tripUpdate, String agencyId, ServiceDate serviceDate) {
        // TODO: Handle unscheduled trip
        LOG.warn("Unscheduled trips are currently unsupported. Skipping TripUpdate.");
        return false;
    }
    
    protected boolean handleCanceledTrip(TripUpdate tripUpdate, String agencyId, ServiceDate serviceDate) {
        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        AgencyAndId tripId = new AgencyAndId(agencyId, tripDescriptor.getTripId());
        TripPattern pattern = getPatternForTripId(tripId);
        
        if (pattern == null) {
            LOG.warn("No pattern found for tripId {}, skipping TripUpdate.", tripId);
            return false;
        }
        
        return this.buffer.update(pattern, tripUpdate, agencyId, this.timeZone, serviceDate);
    }
    
    protected boolean handleReplacementTrip(TripUpdate tripUpdate, String agencyId, ServiceDate serviceDate) {
        // TODO: Handle replacement trip
        LOG.warn("Replacement trips are currently unsupported. Skipping TripUpdate.");
        return false;
    }
    
    protected boolean purgeExpiredData() {
        ServiceDate today = new ServiceDate();
        ServiceDate previously = today.previous().previous(); // Just to be safe...
        
        if ((this.lastPurgeDate != null) && (this.lastPurgeDate.compareTo(previously) > 0)) { return false; }
        
        LOG.debug("purging expired realtime data");
        // TODO: purge expired realtime data
        
        this.lastPurgeDate = previously;
        
        return this.buffer.purgeExpiredData(previously);
    }
    
    protected TripPattern getPatternForTripId(AgencyAndId tripId) {
        Trip trip = this.graphIndex.tripForId.get(tripId);
        TripPattern pattern = this.graphIndex.patternForTrip.get(trip);
        return pattern;
    }
}
