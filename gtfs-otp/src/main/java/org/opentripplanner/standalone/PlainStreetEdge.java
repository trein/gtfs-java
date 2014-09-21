package org.opentripplanner.standalone;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * This represents a street segment.
 *
 * @author novalis
 */
public class PlainStreetEdge extends StreetEdge implements Cloneable {
    
    private static Logger LOG = LoggerFactory.getLogger(PlainStreetEdge.class);
    
    private static final long serialVersionUID = 1L;
    
    private static final double GREENWAY_SAFETY_FACTOR = 0.1;
    
    private ElevationProfileSegment elevationProfileSegment;
    
    private double length;
    
    private LineString geometry;

    private String name;
    
    private String label;

    private boolean wheelchairAccessible = true;
    
    private StreetTraversalPermission permission;
    
    private int streetClass = CLASS_OTHERPATH;

    /**
     * Marks that this edge is the reverse of the one defined in the source data. Does NOT mean
     * fromv/tov are reversed.
     */
    private boolean back;

    private boolean roundabout = false;

    private Set<Alert> notes;
    
    private boolean hasBogusName;
    
    private boolean noThruTraffic;
    
    /**
     * This street is a staircase
     */
    private boolean stairs;

    /**
     * The speed (meters / sec) at which an automobile can traverse this street segment.
     */
    private float carSpeed;

    /** This street has a toll */
    private boolean toll;
    
    private Set<Alert> wheelchairNotes;
    
    private List<TurnRestriction> turnRestrictions = Collections.emptyList();
    
    /** 0 -> 360 degree angle - the angle at the start of the edge geometry */
    public int inAngle;
    
    /** 0 -> 360 degree angle - the angle at the end of the edge geometry */
    public int outAngle;
    
    /**
     * No-arg constructor used only for customization -- do not call this unless you know what you
     * are doing
     */
    public PlainStreetEdge() {
        super(null, null);
    }
    
    public PlainStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, String name, double length,
            StreetTraversalPermission permission, boolean back) {
        // use a default car speed of ~25 mph for splitter vertices and the like
        // TODO(flamholz): do something smarter with the car speed here.
        this(v1, v2, geometry, name, length, permission, back, 11.2f);
    }
    
    public PlainStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, String name, double length,
            StreetTraversalPermission permission, boolean back, float carSpeed) {
        super(v1, v2);
        this.setGeometry(geometry);
        this.length = length;
        this.elevationProfileSegment = new ElevationProfileSegment(length);
        this.name = name;
        this.setPermission(permission);
        this.setBack(back);
        this.setCarSpeed(carSpeed);
        if (geometry != null) {
            try {
                for (Coordinate c : geometry.getCoordinates()) {
                    if (Double.isNaN(c.x)) {
                        System.out.println("X DOOM");
                    }
                    if (Double.isNaN(c.y)) {
                        System.out.println("Y DOOM");
                    }
                }
                double angleR = DirectionUtils.getLastAngle(geometry);
                this.outAngle = ((int) Math.toDegrees(angleR) + 180) % 360;
                angleR = DirectionUtils.getFirstAngle(geometry);
                this.inAngle = ((int) Math.toDegrees(angleR) + 180) % 360;
            } catch (IllegalArgumentException iae) {
                LOG.error("exception while determining street edge angles. setting to zero. there is probably something wrong with this street segment's geometry.");
                this.inAngle = 0;
                this.outAngle = 0;
            }
        }
    }
    
    @Override
    public boolean canTraverse(RoutingRequest options) {
        if (options.wheelchairAccessible) {
            if (!isWheelchairAccessible()) { return false; }
            if (this.elevationProfileSegment.getMaxSlope() > options.maxSlope) { return false; }
        }

        return canTraverse(options.modes);
    }

    @Override
    public boolean canTraverse(TraverseModeSet modes) {
        return getPermission().allows(modes);
    }

    private boolean canTraverse(RoutingRequest options, TraverseMode mode) {
        if (options.wheelchairAccessible) {
            if (!isWheelchairAccessible()) { return false; }
            if (this.elevationProfileSegment.getMaxSlope() > options.maxSlope) { return false; }
        }
        return getPermission().allows(mode);
    }
    
    @Override
    public PackedCoordinateSequence getElevationProfile() {
        return this.elevationProfileSegment.getElevationProfile();
    }
    
    @Override
    public boolean setElevationProfile(PackedCoordinateSequence elev, boolean computed) {
        return this.elevationProfileSegment.setElevationProfile(elev, computed, getPermission().allows(
                StreetTraversalPermission.CAR));
    }
    
    @Override
    public boolean isElevationFlattened() {
        return this.elevationProfileSegment.isFlattened();
    }
    
    @Override
    public double getDistance() {
        return this.length;
    }
    
    @Override
    public State traverse(State s0) {
        final RoutingRequest options = s0.getOptions();
        final TraverseMode currMode = s0.getNonTransitMode();
        StateEditor editor = doTraverse(s0, options, s0.getNonTransitMode());
        State state = (editor == null) ? null : editor.makeState();
        /*
         * Kiss and ride support. Mode transitions occur without the explicit loop edges used in
         * park-and-ride.
         */
        if (options.kissAndRide) {
            if (options.arriveBy) {
                // Branch search to "unparked" CAR mode ASAP after transit has been used.
                // Final WALK check prevents infinite recursion.
                if (s0.isCarParked() && s0.isEverBoarded() && (currMode == TraverseMode.WALK)) {
                    editor = doTraverse(s0, options, TraverseMode.CAR);
                    if (editor != null) {
                        editor.setCarParked(false); // Also has the effect of switching to CAR
                        State forkState = editor.makeState();
                        if (forkState != null) {
                            forkState.addToExistingResultChain(state);
                            return forkState; // return both parked and unparked states
                        }
                    }
                }
            } else { /* departAfter */
                // Irrevocable transition from driving to walking. "Parking" means being dropped off
                // in this case.
                // Final CAR check needed to prevent infinite recursion.
                if (!s0.isCarParked() && !getPermission().allows(TraverseMode.CAR) && (currMode == TraverseMode.CAR)) {
                    editor = doTraverse(s0, options, TraverseMode.WALK);
                    if (editor != null) {
                        editor.setCarParked(true); // has the effect of switching to WALK and
                                                   // preventing further car use
                        return editor.makeState(); // return only the "parked" walking state
                    }
                    
                }
            }
        }
        return state;
    }
    
    /**
     * return a StateEditor rather than a State so that we can make parking/mode switch
     * modifications for kiss-and-ride.
     */
    private StateEditor doTraverse(State s0, RoutingRequest options, TraverseMode traverseMode) {
        boolean walkingBike = options.walkingBike;
        boolean backWalkingBike = s0.isBackWalkingBike();
        TraverseMode backMode = s0.getBackMode();
        Edge backEdge = s0.getBackEdge();
        if (backEdge != null) {
            // No illegal U-turns.
            // NOTE(flamholz): we check both directions because both edges get a chance to decide
            // if they are the reverse of the other. Also, because it doesn't matter which direction
            // we are searching in - these traversals are always disallowed (they are U-turns in one
            // direction
            // or the other).
            // TODO profiling indicates that this is a hot spot.
            if (this.isReverseOf(backEdge) || backEdge.isReverseOf(this)) { return null; }
        }
        
        // Ensure we are actually walking, when walking a bike
        backWalkingBike &= TraverseMode.WALK.equals(backMode);
        walkingBike &= TraverseMode.WALK.equals(traverseMode);
        
        /*
         * Check whether this street allows the current mode. If not and we are biking, attempt to
         * walk the bike.
         */
        if (!canTraverse(options, traverseMode)) {
            if (traverseMode == TraverseMode.BICYCLE) { return doTraverse(s0, options.bikeWalkingOptions, TraverseMode.WALK); }
            return null;
        }
        
        // Automobiles have variable speeds depending on the edge type
        double speed = calculateSpeed(options, traverseMode);

        double time = this.length / speed;
        double weight;
        // TODO(flamholz): factor out this bike, wheelchair and walking specific logic to somewhere
        // central.
        if (options.wheelchairAccessible) {
            weight = this.elevationProfileSegment.getSlopeSpeedEffectiveLength() / speed;
        } else if (traverseMode.equals(TraverseMode.BICYCLE)) {
            time = this.elevationProfileSegment.getSlopeSpeedEffectiveLength() / speed;
            switch (options.optimize) {
                case SAFE:
                    weight = this.elevationProfileSegment.getBicycleSafetyEffectiveLength() / speed;
                    break;
                case GREENWAYS:
                    weight = this.elevationProfileSegment.getBicycleSafetyEffectiveLength() / speed;
                    if ((this.elevationProfileSegment.getBicycleSafetyEffectiveLength() / this.length) <= GREENWAY_SAFETY_FACTOR) {
                        // greenways are treated as even safer than they really are
                        weight *= 0.66;
                    }
                    break;
                case FLAT:
                    /* see notes in StreetVertex on speed overhead */
                    weight = (this.length / speed) + this.elevationProfileSegment.getSlopeWorkCost();
                    break;
                case QUICK:
                    weight = this.elevationProfileSegment.getSlopeSpeedEffectiveLength() / speed;
                    break;
                case TRIANGLE:
                    double quick = this.elevationProfileSegment.getSlopeSpeedEffectiveLength();
                    double safety = this.elevationProfileSegment.getBicycleSafetyEffectiveLength();
                    double slope = this.elevationProfileSegment.getSlopeWorkCost();
                    weight = (quick * options.triangleTimeFactor) + (slope * options.triangleSlopeFactor)
                            + (safety * options.triangleSafetyFactor);
                    weight /= speed;
                    break;
                default:
                    weight = this.length / speed;
            }
        } else {
            if (walkingBike) {
                // take slopes into account when walking bikes
                time = this.elevationProfileSegment.getSlopeSpeedEffectiveLength() / speed;
            }
            weight = time;
            if (traverseMode.equals(TraverseMode.WALK)) {
                // take slopes into account when walking
                // FIXME: this causes steep stairs to be avoided. see #1297.
                double costs = ElevationUtils.getWalkCostsForSlope(this.length, this.elevationProfileSegment.getMaxSlope());
                // as the cost walkspeed is assumed to be for 4.8km/h (= 1.333 m/sec) we need to
                // adjust
                // for the walkspeed set by the user
                double elevationUtilsSpeed = 4.0 / 3.0;
                weight = costs * (elevationUtilsSpeed / speed);
                time = weight; // treat cost as time, as in the current model it actually is the
                               // same (this can be checked for maxSlope == 0)
                /*
                 * // debug code if(weight > 100){ double timeflat = length / speed;
                 * System.out.format(
                 * "line length: %.1f m, slope: %.3f ---> slope costs: %.1f , weight: %.1f , time (flat):  %.1f %n"
                 * , length, elevationProfileSegment.getMaxSlope(), costs, weight, timeflat); }
                 */
            }
        }
        
        if (isStairs()) {
            weight *= options.stairsReluctance;
        } else {
            // TODO: this is being applied even when biking or driving.
            weight *= options.walkReluctance;
        }
        
        StateEditor s1 = s0.edit(this);
        s1.setBackMode(traverseMode);
        s1.setBackWalkingBike(walkingBike);
        
        if ((getWheelchairNotes() != null) && options.wheelchairAccessible) {
            s1.addAlerts(getWheelchairNotes());
        }
        
        /* Compute turn cost. */
        PlainStreetEdge backPSE;
        if ((backEdge != null) && (backEdge instanceof PlainStreetEdge)) {
            backPSE = (PlainStreetEdge) backEdge;
            RoutingRequest backOptions = backWalkingBike ? s0.getOptions().bikeWalkingOptions : s0.getOptions();
                    double backSpeed = backPSE.calculateSpeed(backOptions, backMode);
                    final double realTurnCost; // Units are seconds.
            
            // Apply turn restrictions
                    if (options.arriveBy && !canTurnOnto(backPSE, s0, backMode)) {
                        return null;
                    } else if (!options.arriveBy && !backPSE.canTurnOnto(this, s0, traverseMode)) { return null; }
            
            /*
             * This is a subtle piece of code. Turn costs are evaluated differently during forward
             * and reverse traversal. During forward traversal of an edge, the turn *into* that edge
             * is used, while during reverse traversal, the turn *out of* the edge is used. However,
             * over a set of edges, the turn costs must add up the same (for general correctness and
             * specifically for reverse optimization). This means that during reverse traversal, we
             * must also use the speed for the mode of the backEdge, rather than of the current
             * edge.
             */
                    if (options.arriveBy && (this.tov instanceof IntersectionVertex)) { // arrive-by search
                        IntersectionVertex traversedVertex = ((IntersectionVertex) this.tov);
                
                realTurnCost = backOptions.getIntersectionTraversalCostModel().computeTraversalCost(traversedVertex, this,
                        backPSE, backMode, backOptions, (float) speed, (float) backSpeed);
                    } else if (!options.arriveBy && (this.fromv instanceof IntersectionVertex)) { // depart-after
                                                                                          // search
                        IntersectionVertex traversedVertex = ((IntersectionVertex) this.fromv);
                
                realTurnCost = options.getIntersectionTraversalCostModel().computeTraversalCost(traversedVertex, backPSE, this,
                        traverseMode, options, (float) backSpeed, (float) speed);
            } else {
                        // In case this is a temporary edge not connected to an IntersectionVertex
                        LOG.debug("Not computing turn cost for edge {}", this);
                        realTurnCost = 0;
            }
            
            if (!traverseMode.isDriving()) {
                        s1.incrementWalkDistance(realTurnCost / 100); // just a tie-breaker
                    }
            
            long turnTime = (long) Math.ceil(realTurnCost);
                    time += turnTime;
                    weight += options.turnReluctance * realTurnCost;
        }
        
        if (walkingBike || TraverseMode.BICYCLE.equals(traverseMode)) {
            if (!(backWalkingBike || TraverseMode.BICYCLE.equals(backMode))) {
                s1.incrementTimeInSeconds(options.bikeSwitchTime);
                s1.incrementWeight(options.bikeSwitchCost);
            }
        }
        
        if (!traverseMode.isDriving()) {
            s1.incrementWalkDistance(this.length);
        }
        
        /* On the pre-kiss/pre-park leg, limit both walking and driving, either soft or hard. */
        int roundedTime = (int) Math.ceil(time);
        if (options.kissAndRide || options.parkAndRide) {
            if (options.arriveBy) {
                if (!s0.isCarParked()) {
                    s1.incrementPreTransitTime(roundedTime);
                }
            } else {
                if (!s0.isEverBoarded()) {
                    s1.incrementPreTransitTime(roundedTime);
                }
            }
            if (s1.isMaxPreTransitTimeExceeded(options)) {
                if (options.softPreTransitLimiting) {
                    weight += calculateOverageWeight(s0.getPreTransitTime(), s1.getPreTransitTime(), options.maxPreTransitTime,
                            options.preTransitPenalty, options.preTransitOverageRate);
                } else {
                    return null;
                }
            }
        }

        /*
         * Apply a strategy for avoiding walking too far, either soft (weight increases) or hard
         * limiting (pruning).
         */
        if (s1.weHaveWalkedTooFar(options)) {
            
            // if we're using a soft walk-limit
            if (options.softWalkLimiting) {
                // just slap a penalty for the overage onto s1
                weight += calculateOverageWeight(s0.getWalkDistance(), s1.getWalkDistance(), options.getMaxWalkDistance(),
                        options.softWalkPenalty, options.softWalkOverageRate);
            } else {
                // else, it's a hard limit; bail
                LOG.debug("Too much walking. Bailing.");
                return null;
            }
        }
        
        s1.incrementTimeInSeconds(roundedTime);

        s1.incrementWeight(weight);

        s1.addAlerts(getNotes());

        if (this.isToll() && traverseMode.isDriving()) {
            s1.addAlert(Alert.createSimpleAlerts("Toll road"));
        }

        return s1;
    }
    
    private double calculateOverageWeight(double firstValue, double secondValue, double maxValue, double softPenalty,
            double overageRate) {
        // apply penalty if we stepped over the limit on this traversal
        boolean applyPenalty = false;
        double overageValue;
        
        if ((firstValue <= maxValue) && (secondValue > maxValue)) {
            applyPenalty = true;
            overageValue = secondValue - maxValue;
        } else {
            overageValue = secondValue - firstValue;
        }
        
        // apply overage and add penalty if necessary
        return (overageRate * overageValue) + (applyPenalty ? softPenalty : 0.0);
    }
    
    /**
     * Calculate the average automobile traversal speed of this segment, given the RoutingRequest,
     * and return it in meters per second.
     */
    private double calculateCarSpeed(RoutingRequest options) {
        return getCarSpeed();
    }

    /**
     * Calculate the speed appropriately given the RoutingRequest and traverseMode.
     */
    private double calculateSpeed(RoutingRequest options, TraverseMode traverseMode) {
        if (traverseMode == null) {
            return Double.NaN;
        } else if (traverseMode.isDriving()) {
            // NOTE: Automobiles have variable speeds depending on the edge type
            return calculateCarSpeed(options);
        }
        return options.getSpeed(traverseMode);
    }
    
    @Override
    public double weightLowerBound(RoutingRequest options) {
        return timeLowerBound(options) * options.walkReluctance;
    }
    
    @Override
    public double timeLowerBound(RoutingRequest options) {
        return this.length / options.getStreetSpeedUpperBound();
    }
    
    public void setSlopeSpeedEffectiveLength(double slopeSpeedEffectiveLength) {
        this.elevationProfileSegment.setSlopeSpeedEffectiveLength(slopeSpeedEffectiveLength);
    }
    
    public double getSlopeSpeedEffectiveLength() {
        return this.elevationProfileSegment.getSlopeSpeedEffectiveLength();
    }
    
    public void setSlopeWorkCost(double slopeWorkCost) {
        this.elevationProfileSegment.setSlopeWorkCost(slopeWorkCost);
    }
    
    public double getWorkCost() {
        return this.elevationProfileSegment.getSlopeWorkCost();
    }
    
    public void setBicycleSafetyEffectiveLength(double bicycleSafetyEffectiveLength) {
        this.elevationProfileSegment.setBicycleSafetyEffectiveLength(bicycleSafetyEffectiveLength);
    }
    
    public double getBicycleSafetyEffectiveLength() {
        return this.elevationProfileSegment.getBicycleSafetyEffectiveLength();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    @Override
    public PackedCoordinateSequence getElevationProfile(double start, double end) {
        return this.elevationProfileSegment.getElevationProfile(start, end);
    }
    
    public void setSlopeOverride(boolean slopeOverride) {
        this.elevationProfileSegment.setSlopeOverride(slopeOverride);
    }

    public void setNote(Set<Alert> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "PlainStreetEdge(" + getId() + ", " + this.name + ", " + this.fromv + " -> " + this.tov + " length="
                + this.getLength() + " carSpeed=" + this.getCarSpeed() + " permission=" + this.getPermission() + ")";
    }
    
    @Override
    public boolean hasBogusName() {
        return this.hasBogusName;
    }
    
    /** Returns true if there are any turn restrictions defined. */
    public boolean hasExplicitTurnRestrictions() {
        return (this.turnRestrictions != null) && (this.turnRestrictions.size() > 0);
    }
    
    public void setWheelchairNote(Set<Alert> wheelchairNotes) {
        this.wheelchairNotes = wheelchairNotes;
    }
    
    public void addTurnRestriction(TurnRestriction turnRestriction) {
        if (this.turnRestrictions.isEmpty()) {
            this.turnRestrictions = new ArrayList<TurnRestriction>();
        }
        this.turnRestrictions.add(turnRestriction);
    }
    
    @Override
    public PlainStreetEdge clone() {
        try {
            return (PlainStreetEdge) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canTurnOnto(Edge e, State state, TraverseMode mode) {
        for (TurnRestriction restriction : this.turnRestrictions) {
            /*
             * FIXME: This is wrong for trips that end in the middle of restriction.to
             */
            
            // NOTE(flamholz): edge to be traversed decides equivalence. This is important since
            // it might be a temporary edge that is equivalent to some graph edge.
            if (restriction.type == TurnRestrictionType.ONLY_TURN) {
                if (!e.isEquivalentTo(restriction.to) && restriction.modes.contains(mode)
                        && restriction.active(state.getTimeSeconds())) { return false; }
            } else {
                if (e.isEquivalentTo(restriction.to) && restriction.modes.contains(mode)
                        && restriction.active(state.getTimeSeconds())) { return false; }
            }
        }
        return true;
    }
    
    @Override
    public ElevationProfileSegment getElevationProfileSegment() {
        return this.elevationProfileSegment;
    }
    
    @Override
    protected boolean detachFrom() {
        if (this.fromv != null) {
            for (Edge e : this.fromv.getIncoming()) {
                if (!(e instanceof PlainStreetEdge)) {
                    continue;
                }
                PlainStreetEdge pse = (PlainStreetEdge) e;
                ArrayList<TurnRestriction> restrictions = new ArrayList<TurnRestriction>(pse.turnRestrictions);
                for (TurnRestriction restriction : restrictions) {
                    if (restriction.to == this) {
                        pse.turnRestrictions.remove(restriction);
                    }
                }
            }
        }
        return super.detachFrom();
    }
    
    @Override
    public double getLength() {
        return this.length;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public LineString getGeometry() {
        return this.geometry;
    }
    
    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }
    
    @Override
    public String getLabel() {
        return this.label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public boolean isWheelchairAccessible() {
        return this.wheelchairAccessible;
    }
    
    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }
    
    @Override
    public StreetTraversalPermission getPermission() {
        return this.permission;
    }
    
    public void setPermission(StreetTraversalPermission permission) {
        this.permission = permission;
    }
    
    @Override
    public int getStreetClass() {
        return this.streetClass;
    }
    
    public void setStreetClass(int streetClass) {
        this.streetClass = streetClass;
    }
    
    public boolean isBack() {
        return this.back;
    }
    
    public void setBack(boolean back) {
        this.back = back;
    }
    
    @Override
    public boolean isRoundabout() {
        return this.roundabout;
    }
    
    public void setRoundabout(boolean roundabout) {
        this.roundabout = roundabout;
    }
    
    @Override
    public Set<Alert> getNotes() {
        return this.notes;
    }
    
    public void setHasBogusName(boolean hasBogusName) {
        this.hasBogusName = hasBogusName;
    }
    
    @Override
    public boolean isNoThruTraffic() {
        return this.noThruTraffic;
    }
    
    public void setNoThruTraffic(boolean noThruTraffic) {
        this.noThruTraffic = noThruTraffic;
    }
    
    public boolean isStairs() {
        return this.stairs;
    }
    
    public void setStairs(boolean stairs) {
        this.stairs = stairs;
    }
    
    @Override
    public float getCarSpeed() {
        return this.carSpeed;
    }
    
    @Override
    public void setCarSpeed(float carSpeed) {
        this.carSpeed = carSpeed;
    }
    
    public boolean isToll() {
        return this.toll;
    }
    
    public void setToll(boolean toll) {
        this.toll = toll;
    }
    
    @Override
    public Set<Alert> getWheelchairNotes() {
        return this.wheelchairNotes;
    }
    
    @Override
    public List<TurnRestriction> getTurnRestrictions() {
        return this.turnRestrictions;
    }
    
    @Override
    public int getInAngle() {
        return this.inAngle;
    }
    
    @Override
    public int getOutAngle() {
        return this.outAngle;
    }
    
}
