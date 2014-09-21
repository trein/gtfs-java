package org.opentripplanner.standalone;


/**
 * A Euclidean remaining weight strategy that takes into account transit boarding costs where
 * applicable.
 */
public class DefaultRemainingWeightHeuristic implements RemainingWeightHeuristic {
    
    private static final long serialVersionUID = -5172878150967231550L;
    
    private RoutingRequest options;
    
    private boolean useTransit = false;
    
    private double maxSpeed;
    
    private final DistanceLibrary distanceLibrary = SphericalDistanceLibrary.getInstance();
    
    private TransitLocalStreetService localStreetService;
    
    private double targetX;
    
    private double targetY;
    
    @Override
    public void initialize(State s, Vertex target, long abortTime) {
        this.options = s.getOptions();
        this.useTransit = this.options.modes.isTransit();
        this.maxSpeed = getMaxSpeed(this.options);
        
        Graph graph = this.options.rctx.graph;
        this.localStreetService = graph.getService(TransitLocalStreetService.class);
        
        this.targetX = target.getX();
        this.targetY = target.getY();
    }
    
    /**
     * On a non-transit trip, the remaining weight is simply distance / speed. On a transit trip,
     * there are two cases: (1) we're not on a transit vehicle. In this case, there are two possible
     * ways to compute the remaining distance, and we take whichever is smaller: (a) walking
     * distance / walking speed (b) boarding cost + transit distance / transit speed (this is
     * complicated a bit when we know that there is some walking portion of the trip). (2) we are on
     * a transit vehicle, in which case the remaining weight is simply transit distance / transit
     * speed (no need for boarding cost), again considering any mandatory walking.
     */
    @Override
    public double computeForwardWeight(State s, Vertex target) {
        Vertex sv = s.getVertex();
        double euclideanDistance = this.distanceLibrary.fastDistance(sv.getY(), sv.getX(), this.targetY, this.targetX);
        if (this.useTransit) {
            double streetSpeed = this.options.getStreetSpeedUpperBound();
            if (euclideanDistance < target.getDistanceToNearestTransitStop()) { return (this.options.walkReluctance * euclideanDistance)
                    / streetSpeed; }
            // Search allows using transit, passenger is not alighted local and is not within
            // mandatory walking distance of the target: It is possible we will reach the
            // destination using transit. Find lower bound on cost of this hypothetical trip.
            int boardCost;
            if (s.isOnboard()) {
                // onboard: we might not need any more boardings (remember this is a lower bound).
                boardCost = 0;
            } else {
                // offboard: we know that using transit to reach the destination would require at
                // least one boarding.
                boardCost = this.options.getBoardCostLowerBound();
                if (s.isEverBoarded()) {
                    // the boarding would be a transfer, because we've boarded before.
                    boardCost += this.options.transferPenalty;
                    if (this.localStreetService != null) {
                        if (((this.options.getMaxWalkDistance() - s.getWalkDistance()) < euclideanDistance)
                                && (sv instanceof IntersectionVertex) && !this.localStreetService.transferrable(sv)) { return Double.POSITIVE_INFINITY; }
                    }
                }
            }
            // Find how much mandatory walking is needed to use transit from here.
            // If the passenger is onboard, the second term is zero.
            double mandatoryWalkDistance = target.getDistanceToNearestTransitStop() + sv.getDistanceToNearestTransitStop();
            double transitCost = ((euclideanDistance - mandatoryWalkDistance) / this.maxSpeed) + boardCost;
            double transitStreetCost = (mandatoryWalkDistance * this.options.walkReluctance) / streetSpeed;
            // Compare transit use with the cost of just walking all the way to the destination,
            // and return the lower of the two.
            return Math.min(transitCost + transitStreetCost, (this.options.walkReluctance * euclideanDistance) / streetSpeed);
        } else {
            // search disallows using transit: all travel is on-street
            return (this.options.walkReluctance * euclideanDistance) / this.maxSpeed;
        }
    }
    
    /**
     * computeForwardWeight and computeReverseWeight were identical (except that
     * computeReverseWeight did not have the localStreetService clause). They have been merged.
     */
    @Override
    public double computeReverseWeight(State s, Vertex target) {
        return computeForwardWeight(s, target);
    }
    
    /**
     * Get the maximum expected speed over all modes. This should probably be moved to
     * RoutingRequest.
     */
    public static double getMaxSpeed(RoutingRequest options) {
        if (options.modes.contains(TraverseMode.TRANSIT)) {
            // assume that the max average transit speed over a hop is 10 m/s, which is roughly
            // true in Portland and NYC, but *not* true on highways
            // FIXME this is extremely wrong if you include rail
            return 10;
        } else {
            if (options.optimize == OptimizeType.QUICK) {
                return options.getStreetSpeedUpperBound();
            } else {
                // assume that the best route is no more than 10 times better than
                // the as-the-crow-flies flat base route.
                // FIXME random magic constants
                return options.getStreetSpeedUpperBound() * 10;
            }
        }
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void doSomeWork() {
    }
    
}
