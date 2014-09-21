package org.opentripplanner.standalone;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper around a new State that provides it with setter and increment methods,
 * allowing it to be modified before being put to use. By virtue of being in the same package as
 * States, it can modify their package private fields.
 *
 * @author andrewbyrd
 */
public class StateEditor {
    
    private static final Logger LOG = LoggerFactory.getLogger(StateEditor.class);
    
    protected State child;
    
    private boolean extensionsModified = false;
    
    private boolean spawned = false;
    
    private boolean defectiveTraversal = false;
    
    private boolean traversingBackward;

    // we use our own set of notes and only replace the child notes if they're different
    private Set<Alert> notes = null;
    
    /* CONSTRUCTORS */
    
    protected StateEditor() {
    }

    public StateEditor(RoutingRequest options, Vertex v) {
        this.child = new State(v, options);
    }
    
    public StateEditor(State parent, Edge e) {
        this.child = parent.clone();
        this.child.backState = parent;
        this.child.backEdge = e;
        // We clear child.next here, since it could have already been set in the
        // parent
        this.child.next = null;
        if (e == null) {
            this.child.backState = null;
            this.child.vertex = parent.vertex;
            this.child.stateData = this.child.stateData.clone();
        } else {
            // be clever
            // Note that we use equals(), not ==, here to allow for dynamically
            // created vertices
            if (e.getFromVertex().equals(e.getToVertex()) && parent.vertex.equals(e.getFromVertex())) {
                // TODO LG: We disable this test: the assumption that
                // the from and to vertex of an edge are not the same
                // is not true anymore: bike rental on/off edges.
                this.traversingBackward = parent.getOptions().arriveBy;
                this.child.vertex = e.getToVertex();
            } else if (parent.vertex.equals(e.getFromVertex())) {
                this.traversingBackward = false;
                this.child.vertex = e.getToVertex();
            } else if (parent.vertex.equals(e.getToVertex())) {
                this.traversingBackward = true;
                this.child.vertex = e.getFromVertex();
            } else {
                // Parent state is not at either end of edge.
                LOG.warn("Edge is not connected to parent state: {}", e);
                LOG.warn("   from   vertex: {}", e.getFromVertex());
                LOG.warn("   to     vertex: {}", e.getToVertex());
                LOG.warn("   parent vertex: {}", parent.vertex);
                this.defectiveTraversal = true;
            }
            if (this.traversingBackward != parent.getOptions().arriveBy) {
                LOG.error("Actual traversal direction does not match traversal direction in TraverseOptions.");
                this.defectiveTraversal = true;
            }
        }
    }
    
    /* PUBLIC METHODS */
    
    /**
     * Why can a state editor only be used once? If you modify some component of state with and
     * editor, use the editor to create a new state, and then make more modifications, these
     * modifications will be applied to the previously created state. Reusing the state editor to
     * make several states would modify an existing state somewhere earlier in the search, messing
     * up the shortest path tree.
     */
    public State makeState() {
        // check that this editor has not been used already
        if (this.spawned) { throw new IllegalStateException("A StateEditor can only be used once."); }
        
        // if something was flagged incorrect, do not make a new state
        if (this.defectiveTraversal) {
            LOG.error("Defective traversal flagged on edge " + this.child.backEdge);
            return null;
        }
        
        if (this.child.backState != null) {
            // make it impossible to use a state with lower weight than its
            // parent.
            this.child.checkNegativeWeight();
            
            // check that time changes are coherent with edge traversal
            // direction
            if (this.traversingBackward ? (this.child.getTimeDeltaSeconds() > 0) : (this.child.getTimeDeltaSeconds() < 0)) {
                LOG.trace("Time was incremented the wrong direction during state editing. {}", this.child.backEdge);
                return null;
            }
        }
        if (!parsePath(this.child)) { return null; }
        
        // copy the notes if need be, keeping in mind they may both be null
        if (this.notes != this.child.stateData.notes) {
            cloneStateDataAsNeeded();
            this.child.stateData.notes = this.notes;
        }

        this.spawned = true;
        return this.child;
    }
    
    public boolean weHaveWalkedTooFar(RoutingRequest options) {
        // Only apply limit in transit-only case
        if (!options.modes.isTransit()) { return false; }
        
        return this.child.walkDistance >= options.maxWalkDistance;
    }
    
    public boolean isMaxPreTransitTimeExceeded(RoutingRequest options) {
        return this.child.preTransitTime > options.maxPreTransitTime;
    }
    
    @Override
    public String toString() {
        return "<StateEditor " + this.child + ">";
    }
    
    /* PUBLIC METHODS TO MODIFY A STATE BEFORE IT IS USED */
    
    /**
     * Put a new value into the State extensions map. This will always clone the extensions map
     * before it is modified the first time, making sure that other references to the map in earlier
     * States are unaffected.
     */
    @SuppressWarnings("unchecked")
    public void setExtension(Object key, Object value) {
        cloneStateDataAsNeeded();
        if (!this.extensionsModified) {
            HashMap<Object, Object> newExtensions;
            if (this.child.stateData.extensions == null) {
                newExtensions = new HashMap<Object, Object>(4);
            } else {
                newExtensions = (HashMap<Object, Object>) this.child.stateData.extensions.clone();
            }
            this.child.stateData.extensions = newExtensions;
            this.extensionsModified = true;
        }
        this.child.stateData.extensions.put(key, value);
    }
    
    /**
     * Tell the stateEditor to return null when makeState() is called, no matter what other editing
     * has been done. This allows graph patches to block traversals.
     */
    public void blockTraversal() {
        this.defectiveTraversal = true;
    }
    
    /**
     * Add an alert to this state. This used to use an EdgeNarrative
     */
    public void addAlert(Alert notes) {
        if (notes == null) { return; }

        if (this.notes == null) {
            this.notes = new HashSet<Alert>();
        }

        this.notes.add(notes);
    }

    /**
     * Convenience function to add multiple alerts
     */
    public void addAlerts(Iterable<Alert> alerts) {
        if (alerts == null) { return; }
        for (Alert alert : alerts) {
            this.addAlert(alert);
        }
    }
    
    /* Incrementors */
    
    public void incrementWeight(double weight) {
        if (Double.isNaN(weight)) {
            LOG.warn("A state's weight is being incremented by NaN while traversing edge " + this.child.backEdge);
            this.defectiveTraversal = true;
            return;
        }
        if (weight < 0) {
            LOG.warn("A state's weight is being incremented by a negative amount while traversing edge " + this.child.backEdge);
            this.defectiveTraversal = true;
            return;
        }
        this.child.weight += weight;
    }
    
    /**
     * Advance or rewind the time of the new state by the given non-negative amount. Direction of
     * time is inferred from the direction of traversal. This is the only element of state that runs
     * backward when traversing backward.
     */
    public void incrementTimeInSeconds(int seconds) {
        incrementTimeInMilliseconds(seconds * 1000L);
    }

    public void incrementTimeInMilliseconds(long milliseconds) {
        if (milliseconds < 0) {
            LOG.warn("A state's time is being incremented by a negative amount while traversing edge " + this.child.getBackEdge());
            this.defectiveTraversal = true;
            return;
        }
        this.child.time += (this.traversingBackward ? -milliseconds : milliseconds);
    }
    
    public void incrementWalkDistance(double length) {
        if (length < 0) {
            LOG.warn("A state's walk distance is being incremented by a negative amount.");
            this.defectiveTraversal = true;
            return;
        }
        this.child.walkDistance += length;
    }
    
    public void incrementPreTransitTime(int seconds) {
        if (seconds < 0) {
            LOG.warn("A state's pre-transit time is being incremented by a negative amount.");
            this.defectiveTraversal = true;
            return;
        }
        this.child.preTransitTime += seconds;
    }
    
    public void incrementNumBoardings() {
        cloneStateDataAsNeeded();
        this.child.stateData.numBoardings++;
        setEverBoarded(true);
    }
    
    /* Basic Setters */
    
    public void setTripTimes(TripTimes tripTimes) {
        cloneStateDataAsNeeded();
        this.child.stateData.tripTimes = tripTimes;
    }
    
    public void setTripId(AgencyAndId tripId) {
        cloneStateDataAsNeeded();
        this.child.stateData.tripId = tripId;
    }
    
    public void setPreviousTrip(Trip previousTrip) {
        cloneStateDataAsNeeded();
        this.child.stateData.previousTrip = previousTrip;
    }

    /**
     * Initial wait time is recorded so it can be subtracted out of paths in lieu of
     * "reverse optimization". This happens in Analyst.
     */
    public void setInitialWaitTimeSeconds(long initialWaitTimeSeconds) {
        cloneStateDataAsNeeded();
        this.child.stateData.initialWaitTime = initialWaitTimeSeconds;
    }

    public void setBackMode(TraverseMode mode) {
        if (mode == this.child.stateData.backMode) { return; }

        cloneStateDataAsNeeded();
        this.child.stateData.backMode = mode;
    }
    
    public void setBackWalkingBike(boolean walkingBike) {
        if (walkingBike == this.child.stateData.backWalkingBike) { return; }

        cloneStateDataAsNeeded();
        this.child.stateData.backWalkingBike = walkingBike;
    }
    
    /**
     * The lastNextArrivalDelta is the amount of time between the arrival of the last trip the
     * planner used and the arrival of the trip after that.
     */
    public void setLastNextArrivalDelta(int lastNextArrivalDelta) {
        cloneStateDataAsNeeded();
        this.child.stateData.lastNextArrivalDelta = lastNextArrivalDelta;
    }
    
    public void setWalkDistance(double walkDistance) {
        this.child.walkDistance = walkDistance;
    }
    
    public void setPreTransitTime(int preTransitTime) {
        this.child.preTransitTime = preTransitTime;
    }
    
    public void setZone(String zone) {
        if (zone == null) {
            if (this.child.stateData.zone != null) {
                cloneStateDataAsNeeded();
                this.child.stateData.zone = zone;
            }
        } else if (!zone.equals(this.child.stateData.zone)) {
            cloneStateDataAsNeeded();
            this.child.stateData.zone = zone;
        }
    }
    
    public void setRoute(AgencyAndId routeId) {
        cloneStateDataAsNeeded();
        this.child.stateData.route = routeId;
        // unlike tripId, routeId is not set to null when alighting
        // but do a null check anyway
        if (routeId != null) {
            AgencyAndId[] oldRouteSequence = this.child.stateData.routeSequence;
            // LOG.debug("old route seq {}", Arrays.asList(oldRouteSequence));
            int oldLength = oldRouteSequence.length;
            this.child.stateData.routeSequence = Arrays.copyOf(oldRouteSequence, oldLength + 1);
            this.child.stateData.routeSequence[oldLength] = routeId;
            // LOG.debug("new route seq {}", Arrays.asList(child.stateData.routeSequence)); // array
            // will be interpreted as varargs
        }
    }
    
    public void setNumBoardings(int numBoardings) {
        cloneStateDataAsNeeded();
        this.child.stateData.numBoardings = numBoardings;
    }
    
    public void setEverBoarded(boolean everBoarded) {
        cloneStateDataAsNeeded();
        this.child.stateData.everBoarded = true;
    }
    
    public void setBikeRenting(boolean bikeRenting) {
        cloneStateDataAsNeeded();
        this.child.stateData.usingRentedBike = bikeRenting;
        if (bikeRenting) {
            this.child.stateData.nonTransitMode = TraverseMode.BICYCLE;
        } else {
            this.child.stateData.nonTransitMode = TraverseMode.WALK;
        }
    }
    
    /**
     * This has two effects: marks the car as parked, and switches the current mode. Marking the car
     * parked is important for allowing co-dominance of walking and driving states.
     */
    public void setCarParked(boolean carParked) {
        cloneStateDataAsNeeded();
        this.child.stateData.carParked = carParked;
        if (carParked) {
            // We do not handle mixed-mode P+BIKE...
            this.child.stateData.nonTransitMode = TraverseMode.WALK;
        } else {
            this.child.stateData.nonTransitMode = TraverseMode.CAR;
        }
    }
    
    public void setPreviousStop(Stop previousStop) {
        cloneStateDataAsNeeded();
        this.child.stateData.previousStop = previousStop;
    }
    
    public void setLastAlightedTimeSeconds(long lastAlightedTimeSeconds) {
        cloneStateDataAsNeeded();
        this.child.stateData.lastAlightedTime = lastAlightedTimeSeconds;
    }
    
    public void setTimeSeconds(long seconds) {
        this.child.time = seconds * 1000;
    }
    
    public void setStartTimeSeconds(long seconds) {
        cloneStateDataAsNeeded();
        this.child.stateData.startTime = seconds;
    }
    
    /**
     * Set non-incremental state values (ex. {@link State#getRoute()}) from an existing state.
     * Incremental values (ex. {@link State#getNumBoardings()}) are not currently set.
     *
     * @param state
     */
    public void setFromState(State state) {
        cloneStateDataAsNeeded();
        this.child.stateData.route = state.stateData.route;
        this.child.stateData.tripTimes = state.stateData.tripTimes;
        this.child.stateData.tripId = state.stateData.tripId;
        this.child.stateData.serviceDay = state.stateData.serviceDay;
        this.child.stateData.previousTrip = state.stateData.previousTrip;
        this.child.stateData.previousStop = state.stateData.previousStop;
        this.child.stateData.zone = state.stateData.zone;
        this.child.stateData.extensions = state.stateData.extensions;
        this.child.stateData.usingRentedBike = state.stateData.usingRentedBike;
        this.child.stateData.carParked = state.stateData.carParked;
    }
    
    /* PUBLIC GETTER METHODS */
    
    /*
     * Allow patches to see the State being edited, so they can decide whether to apply their
     * transformations to the traversal result or not.
     */
    
    public Object getExtension(Object key) {
        return this.child.getExtension(key);
    }
    
    public long getTimeSeconds() {
        return this.child.getTimeSeconds();
    }
    
    public long getElapsedTimeSeconds() {
        return this.child.getElapsedTimeSeconds();
    }
    
    public AgencyAndId getTripId() {
        return this.child.getTripId();
    }
    
    public Trip getPreviousTrip() {
        return this.child.getPreviousTrip();
    }

    public String getZone() {
        return this.child.getZone();
    }
    
    public AgencyAndId getRoute() {
        return this.child.getRoute();
    }
    
    public int getNumBoardings() {
        return this.child.getNumBoardings();
    }
    
    public boolean isEverBoarded() {
        return this.child.isEverBoarded();
    }
    
    public boolean isRentingBike() {
        return this.child.isBikeRenting();
    }
    
    public long getLastAlightedTimeSeconds() {
        return this.child.getLastAlightedTimeSeconds();
    }
    
    public double getWalkDistance() {
        return this.child.getWalkDistance();
    }
    
    public int getPreTransitTime() {
        return this.child.getPreTransitTime();
    }
    
    public Vertex getVertex() {
        return this.child.getVertex();
    }
    
    /* PRIVATE METHODS */
    
    /**
     * To be called before modifying anything in the child's StateData. Makes sure that changes are
     * applied to a copy of StateData rather than the same one that is still referenced in existing,
     * older states.
     */
    private void cloneStateDataAsNeeded() {
        if ((this.child.backState != null) && (this.child.stateData == this.child.backState.stateData)) {
            this.child.stateData = this.child.stateData.clone();
        }
    }
    
    /** return true if all PathParsers advanced to a state other than REJECT */
    public boolean parsePath(State state) {
        if (state.stateData.opt.rctx == null) { return true; // a lot of tests don't set a routing
                                                             // context
        }
        PathParser[] parsers = state.stateData.opt.rctx.pathParsers;
        int[] parserStates = state.pathParserStates;
        boolean accept = true;
        boolean modified = false;
        int i = 0;
        for (PathParser parser : parsers) {
            int terminal = parser.terminalFor(state);
            int oldState = parserStates[i];
            int newState = parser.transition(oldState, terminal);
            if (newState != oldState) {
                if (!modified) {
                    // clone the state array so only the new state will see modifications
                    parserStates = parserStates.clone();
                    modified = true;
                }
                parserStates[i] = newState;
                if (newState == AutomatonState.REJECT) {
                    accept = false;
                }
            }
            i++;
        }
        if (modified) {
            state.pathParserStates = parserStates;
        }
        
        return accept;
    }
    
    public void alightTransit() {
        cloneStateDataAsNeeded();
        this.child.stateData.lastTransitWalk = this.child.getWalkDistance();
    }
    
    public void setLastPattern(TripPattern pattern) {
        cloneStateDataAsNeeded();
        this.child.stateData.lastPattern = pattern;
    }
    
    public void setOptions(RoutingRequest options) {
        cloneStateDataAsNeeded();
        this.child.stateData.opt = options;
    }
    
    public void setServiceDay(ServiceDay day) {
        cloneStateDataAsNeeded();
        this.child.stateData.serviceDay = day;
    }
    
    public void setBikeRentalNetwork(Set<String> networks) {
        cloneStateDataAsNeeded();
        this.child.stateData.bikeRentalNetworks = networks;
    }
}
