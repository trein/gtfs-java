package org.opentripplanner.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.transport.jms.MapUtils;
import org.geotools.geometry.Envelope2D;
import org.geotools.util.DisjointSet;
import org.omg.CORBA.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Builds a street graph from OpenStreetMap data.
 */
enum Direction {
    LEFT, RIGHT, U, STRAIGHT;
}

/**
 * A temporary holder for turn restrictions while we have only way/node ids but not yet edge objects
 */
class TurnRestrictionTag {
    long via;
    
    TurnRestrictionType type;
    
    Direction direction;
    
    RepeatingTimePeriod time;
    
    public List<PlainStreetEdge> possibleFrom = new ArrayList<PlainStreetEdge>();
    
    public List<PlainStreetEdge> possibleTo = new ArrayList<PlainStreetEdge>();
    
    public TraverseModeSet modes;
    
    TurnRestrictionTag(long via, TurnRestrictionType type, Direction direction) {
        this.via = via;
        this.type = type;
        this.direction = direction;
    }
    
    @Override
    public String toString() {
        return String.format("%s turn restriction via node %d", this.direction, this.via);
    }
}

public class OpenStreetMapGraphBuilderImpl implements GraphBuilder {
    
    private static Logger LOG = LoggerFactory.getLogger(OpenStreetMapGraphBuilderImpl.class);
    
    // Private members that are only read or written internally.
    
    private final Set<Object> _uniques = new HashSet<Object>();
    
    private final HashMap<Vertex, Double> elevationData = new HashMap<Vertex, Double>();
    
    public boolean skipVisibility = false;
    
    // Members that can be set by clients.
    
    /**
     * WayPropertySet computes edge properties from OSM way data.
     */
    public WayPropertySet wayPropertySet = new WayPropertySet();
    
    /**
     * Providers of OSM data.
     */
    private final List<OpenStreetMapProvider> _providers = new ArrayList<OpenStreetMapProvider>();
    
    /**
     * Allows for arbitrary custom naming of edges.
     */
    public CustomNamer customNamer;

    /**
     * Ignore wheelchair accessibility information.
     */
    public boolean ignoreWheelchairAccessibility = false;
    
    /**
     * Allows for alternate PlainStreetEdge implementations; this is intended for users who want to
     * provide more info in PSE than OTP normally keeps around.
     */
    public OSMPlainStreetEdgeFactory edgeFactory = new DefaultOSMPlainStreetEdgeFactory();
    
    /**
     * If true, disallow zero floors and add 1 to non-negative numeric floors, as is generally done
     * in the United States. This does not affect floor names from level maps.
     */
    public boolean noZeroLevels = true;
    
    /**
     * Whether bike rental stations should be loaded from OSM, rather than periodically dynamically
     * pulled from APIs.
     */
    public boolean staticBikeRental = false;

    /**
     * Whether we should create P+R stations from OSM data.
     */
    public boolean staticParkAndRide = true;
    
    @Override
    public List<String> provides() {
        return Arrays.asList("streets", "turns");
    }
    
    @Override
    public List<String> getPrerequisites() {
        return Collections.emptyList();
    }
    
    /**
     * The source for OSM map data
     */
    public void setProvider(OpenStreetMapProvider provider) {
        this._providers.add(provider);
    }
    
    /**
     * Multiple sources for OSM map data
     */
    public void setProviders(List<OpenStreetMapProvider> providers) {
        this._providers.addAll(providers);
    }
    
    /**
     * Set the way properties from a {@link WayPropertySetSource} source.
     *
     * @param source the way properties source
     */
    public void setDefaultWayPropertySetSource(WayPropertySetSource source) {
        this.wayPropertySet = source.getWayPropertySet();
    }
    
    /**
     * Construct and set providers all at once.
     */
    public OpenStreetMapGraphBuilderImpl(List<OpenStreetMapProvider> providers) {
        this.setProviders(providers);
    }
    
    public OpenStreetMapGraphBuilderImpl() {
    }
    
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        Handler handler = new Handler(graph);
        for (OpenStreetMapProvider provider : this._providers) {
            LOG.info("Gathering OSM from provider: " + provider);
            provider.readOSM(handler);
        }
        LOG.info("Building street graph from OSM");
        handler.buildGraph(extra);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T unique(T value) {
        if (!this._uniques.contains(value)) {
            this._uniques.add(value);
        }
        return value;
    }
    
    private class Handler implements OpenStreetMapContentHandler {
        
        private static final double VISIBILITY_EPSILON = 0.000000001;
        
        private static final String nodeLabelFormat = "osm:node:%d";
        
        private static final String levelnodeLabelFormat = nodeLabelFormat + ":level:%s";
        
        private final Map<Long, OSMNode> _nodes = new HashMap<Long, OSMNode>();
        
        private final Map<Long, OSMWay> _ways = new HashMap<Long, OSMWay>();
        
        private final List<Area> _walkableAreas = new ArrayList<Area>();

        private final List<Area> _parkAndRideAreas = new ArrayList<Area>();
        
        private final Set<Long> _areaWayIds = new HashSet<Long>();
        
        private final Map<Long, OSMWay> _areaWaysById = new HashMap<Long, OSMWay>();
        
        private final Map<Long, Set<OSMWay>> _areasForNode = new HashMap<Long, Set<OSMWay>>();
        
        private final List<OSMWay> _singleWayAreas = new ArrayList<OSMWay>();
        
        private final Map<Long, OSMRelation> _relations = new HashMap<Long, OSMRelation>();
        
        private final Set<OSMWithTags> _processedAreas = new HashSet<OSMWithTags>();
        
        private final Set<Long> _nodesWithNeighbors = new HashSet<Long>();
        
        private final Set<Long> _areaNodes = new HashSet<Long>();
        
        private final Multimap<Long, TurnRestrictionTag> turnRestrictionsByFromWay = ArrayListMultimap.create();
        
        private final Multimap<Long, TurnRestrictionTag> turnRestrictionsByToWay = ArrayListMultimap.create();
        
        private final Map<OSMWithTags, Set<OSMNode>> stopsInAreas = new HashMap<OSMWithTags, Set<OSMNode>>();
        
        class Ring {
            public List<OSMNode> nodes;
            
            public VLPolygon geometry;
            
            public List<Ring> holes = new ArrayList<Ring>();
            
            // equivalent to the ring representation, but used for JTS operations
            private Polygon jtsPolygon;
            
            /**
             * Why is there a boolean parameter called javaSucks? Because otherwise the two
             * constructors have the same erasure, meaning that even though Java has enough
             * information at compile-time to figure out which constructor I am talking about, it
             * intentionally throws this away in the interest of having worse run-time performance.
             * Thanks, Java!
             * <p/>
             * Oh, and most people would solve this problem by making a static factory method but
             * that won't work because then all of this class's outer classes would have to be
             * static.
             *
             * @param osmNodes
             * @param javaSucks
             */
            public Ring(List<OSMNode> osmNodes, boolean javaSucks) {
                ArrayList<VLPoint> vertices = new ArrayList<VLPoint>();
                this.nodes = osmNodes;
                for (OSMNode node : osmNodes) {
                    VLPoint point = new VLPoint(node.lon, node.lat);
                    vertices.add(point);
                }
                this.geometry = new VLPolygon(vertices);
            }
            
            public Ring(List<Long> osmNodes) {
                ArrayList<VLPoint> vertices = new ArrayList<VLPoint>();
                this.nodes = new ArrayList<OSMNode>(osmNodes.size());
                for (long nodeId : osmNodes) {
                    OSMNode node = Handler.this._nodes.get(nodeId);
                    if (this.nodes.contains(node)) {
                        // hopefully, this only happens in order to
                        // close polygons
                        continue;
                    }
                    VLPoint point = new VLPoint(node.lon, node.lat);
                    this.nodes.add(node);
                    vertices.add(point);
                }
                this.geometry = new VLPolygon(vertices);
            }
            
            public Polygon toJtsPolygon() {
                if (this.jtsPolygon != null) { return this.jtsPolygon; }
                GeometryFactory factory = GeometryUtils.getGeometryFactory();
                
                LinearRing shell = factory.createLinearRing(toCoordinates(this.geometry));
                
                // we need to merge connected holes here, because JTS does not believe in
                // holes that touch at multiple points (and, weirdly, does not have a method
                // to detect this other than this crazy DE-9IM stuff
                
                List<Polygon> polygonHoles = new ArrayList<Polygon>();
                for (Ring ring : this.holes) {
                    LinearRing linearRing = factory.createLinearRing(toCoordinates(ring.geometry));
                    Polygon polygon = factory.createPolygon(linearRing, new LinearRing[0]);
                    for (Iterator<Polygon> it = polygonHoles.iterator(); it.hasNext();) {
                        Polygon otherHole = it.next();
                        if (otherHole.relate(polygon, "F***1****")) {
                            polygon = (Polygon) polygon.union(otherHole);
                            it.remove();
                        }
                    }
                    polygonHoles.add(polygon);
                }
                
                ArrayList<LinearRing> lrholelist = new ArrayList<LinearRing>(polygonHoles.size());
                
                for (Polygon hole : polygonHoles) {
                    Geometry boundary = hole.getBoundary();
                    if (boundary instanceof LinearRing) {
                        lrholelist.add((LinearRing) boundary);
                    } else {
                        // this is a case of a hole inside a hole. OSM technically
                        // allows this, but it would be a giant hassle to get right. So:
                        LineString line = hole.getExteriorRing();
                        LinearRing ring = factory.createLinearRing(line.getCoordinates());
                        lrholelist.add(ring);
                    }
                }
                LinearRing[] lrholes = lrholelist.toArray(new LinearRing[lrholelist.size()]);
                this.jtsPolygon = factory.createPolygon(shell, lrholes);
                return this.jtsPolygon;
            }
            
            private Coordinate[] toCoordinates(VLPolygon geometry) {
                Coordinate[] coords = new Coordinate[geometry.n() + 1];
                int i = 0;
                for (VLPoint point : geometry.vertices) {
                    coords[i++] = new Coordinate(point.x, point.y);
                }
                VLPoint first = geometry.vertices.get(0);
                coords[i++] = new Coordinate(first.x, first.y);
                return coords;
            }
        }
        
        /**
         * Stores information about an OSM area needed for visibility graph construction. Algorithm
         * based on http://wiki.openstreetmap.org/wiki/Relation:multipolygon/Algorithm but generally
         * done in a quick/dirty way.
         */
        class Area {
            
            public class AreaConstructionException extends RuntimeException {
                private static final long serialVersionUID = 1L;
            }
            
            OSMWithTags parent; // this is the way or relation that has the relevant tags for the
            // area
            
            List<Ring> outermostRings = new ArrayList<Ring>();
            
            private MultiPolygon jtsMultiPolygon;
            
            Area(OSMWithTags parent, List<OSMWay> outerRingWays, List<OSMWay> innerRingWays) {
                this.parent = parent;
                setWayName(parent);
                // ring assignment
                List<List<Long>> innerRingNodes = constructRings(innerRingWays);
                List<List<Long>> outerRingNodes = constructRings(outerRingWays);
                if ((innerRingNodes == null) || (outerRingNodes == null)) { throw new AreaConstructionException(); }
                ArrayList<List<Long>> allRings = new ArrayList<List<Long>>(innerRingNodes);
                allRings.addAll(outerRingNodes);
                
                List<Ring> innerRings = new ArrayList<Ring>();
                List<Ring> outerRings = new ArrayList<Ring>();
                for (List<Long> ring : innerRingNodes) {
                    innerRings.add(new Ring(ring));
                }
                for (List<Long> ring : outerRingNodes) {
                    outerRings.add(new Ring(ring));
                }
                
                // now, ring grouping
                // first, find outermost rings
                OUTER: for (Ring outer : outerRings) {
                        for (Ring possibleContainer : outerRings) {
                            if ((outer != possibleContainer) && outer.geometry.hasPointInside(possibleContainer.geometry)) {
                                continue OUTER;
                            }
                        }
                        this.outermostRings.add(outer);
                    
                    // find holes in this ring
                        for (Ring possibleHole : innerRings) {
                            if (possibleHole.geometry.hasPointInside(outer.geometry)) {
                                outer.holes.add(possibleHole);
                            }
                        }
                    }
                // run this at end of ctor so that exception
                // can be caught in the right place
                toJTSMultiPolygon();
            }
            
            public MultiPolygon toJTSMultiPolygon() {
                if (this.jtsMultiPolygon == null) {
                    List<Polygon> polygons = new ArrayList<Polygon>();
                    for (Ring ring : this.outermostRings) {
                        polygons.add(ring.toJtsPolygon());
                    }
                    this.jtsMultiPolygon = GeometryUtils.getGeometryFactory()
                            .createMultiPolygon(polygons.toArray(new Polygon[0]));
                    if (!this.jtsMultiPolygon.isValid()) { throw new AreaConstructionException(); }
                }
                
                return this.jtsMultiPolygon;
            }
            
            public List<List<Long>> constructRings(List<OSMWay> ways) {
                if (ways.size() == 0) {
                    // no rings is no rings
                    return Collections.emptyList();
                }
                
                List<List<Long>> closedRings = new ArrayList<List<Long>>();
                
                ArrayListMultimap<Long, OSMWay> waysByEndpoint = ArrayListMultimap.create();
                for (OSMWay way : ways) {
                    List<Long> refs = way.getNodeRefs();
                    
                    long start = refs.get(0);
                    long end = refs.get(refs.size() - 1);
                    if (start == end) {
                        ArrayList<Long> ring = new ArrayList<Long>(refs);
                        closedRings.add(ring);
                    } else {
                        waysByEndpoint.put(start, way);
                        waysByEndpoint.put(end, way);
                    }
                }
                
                // precheck for impossible situations
                List<Long> toRemove = new ArrayList<Long>();
                for (Long endpoint : waysByEndpoint.keySet()) {
                    Collection<OSMWay> list = waysByEndpoint.get(endpoint);
                    if ((list.size() % 2) == 1) { return null; }
                }
                for (Long key : toRemove) {
                    waysByEndpoint.removeAll(key);
                }
                
                List<Long> partialRing = new ArrayList<Long>();
                if (waysByEndpoint.size() == 0) { return closedRings; }
                
                long firstEndpoint = 0, otherEndpoint = 0;
                OSMWay firstWay = null;
                for (Long endpoint : waysByEndpoint.keySet()) {
                    List<OSMWay> list = waysByEndpoint.get(endpoint);
                    firstWay = list.get(0);
                    List<Long> nodeRefs = firstWay.getNodeRefs();
                    partialRing.addAll(nodeRefs);
                    firstEndpoint = nodeRefs.get(0);
                    otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                    break;
                }
                waysByEndpoint.get(firstEndpoint).remove(firstWay);
                waysByEndpoint.get(otherEndpoint).remove(firstWay);
                if (constructRingsRecursive(waysByEndpoint, partialRing, closedRings, firstEndpoint)) {
                    return closedRings;
                } else {
                    return null;
                }
            }
            
            private boolean constructRingsRecursive(ArrayListMultimap<Long, OSMWay> waysByEndpoint, List<Long> ring,
                    List<List<Long>> closedRings, long endpoint) {
                
                List<OSMWay> ways = new ArrayList<OSMWay>(waysByEndpoint.get(endpoint));
                
                for (OSMWay way : ways) {
                    // remove this way from the map
                    List<Long> nodeRefs = way.getNodeRefs();
                    long firstEndpoint = nodeRefs.get(0);
                    long otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                    
                    waysByEndpoint.remove(firstEndpoint, way);
                    waysByEndpoint.remove(otherEndpoint, way);
                    
                    ArrayList<Long> newRing = new ArrayList<Long>(ring.size() + nodeRefs.size());
                    long newFirstEndpoint;
                    if (firstEndpoint == endpoint) {
                        for (int j = nodeRefs.size() - 1; j >= 1; --j) {
                            newRing.add(nodeRefs.get(j));
                        }
                        newRing.addAll(ring);
                        newFirstEndpoint = otherEndpoint;
                    } else {
                        newRing.addAll(nodeRefs.subList(0, nodeRefs.size() - 1));
                        newRing.addAll(ring);
                        newFirstEndpoint = firstEndpoint;
                    }
                    if (newRing.get(newRing.size() - 1).equals(newRing.get(0))) {
                        // ring closure
                        closedRings.add(newRing);
                        // if we're out of endpoints, then we have succeeded
                        if (waysByEndpoint.size() == 0) { return true; // success
                        }
                        
                        // otherwise, we need to start a new partial ring
                        newRing = new ArrayList<Long>();
                        OSMWay firstWay = null;
                        for (Long entry : waysByEndpoint.keySet()) {
                            List<OSMWay> list = waysByEndpoint.get(entry);
                            firstWay = list.get(0);
                            nodeRefs = firstWay.getNodeRefs();
                            newRing.addAll(nodeRefs);
                            firstEndpoint = nodeRefs.get(0);
                            otherEndpoint = nodeRefs.get(nodeRefs.size() - 1);
                            break;
                        }
                        
                        waysByEndpoint.remove(firstEndpoint, firstWay);
                        waysByEndpoint.remove(otherEndpoint, firstWay);
                        
                        if (constructRingsRecursive(waysByEndpoint, newRing, closedRings, firstEndpoint)) { return true; }
                        
                        waysByEndpoint.remove(firstEndpoint, firstWay);
                        waysByEndpoint.remove(otherEndpoint, firstWay);
                        
                    } else {
                        // continue with this ring
                        if (waysByEndpoint.get(newFirstEndpoint) != null) {
                            if (constructRingsRecursive(waysByEndpoint, newRing, closedRings, newFirstEndpoint)) { return true; }
                        }
                    }
                    if (firstEndpoint == endpoint) {
                        waysByEndpoint.put(otherEndpoint, way);
                    } else {
                        waysByEndpoint.put(firstEndpoint, way);
                    }
                }
                return false;
            }
        }
        
        /**
         * A group of possibly-contiguous areas sharing the same level
         */
        class AreaGroup {
            /*
             * The list of underlying areas, used when generating edges out of the visibility graph
             */
            Collection<Area> areas;
            
            /**
             * The joined outermost rings of the areas (with inner rings for holes as necessary).
             */
            List<Ring> outermostRings = new ArrayList<Ring>();
            
            public AreaGroup(Collection<Area> areas) {
                this.areas = areas;
                
                // Merging non-convex polygons is complicated, so we need to convert to JTS, let JTS
                // do the hard work,
                // then convert back.
                List<Polygon> allRings = new ArrayList<Polygon>();
                
                // However, JTS will lose the coord<->osmnode mapping, and we will have to
                // reconstruct it.
                HashMap<Coordinate, OSMNode> nodeMap = new HashMap<Coordinate, OSMNode>();
                for (Area area : areas) {
                    for (Ring ring : area.outermostRings) {
                        allRings.add(ring.toJtsPolygon());
                        for (OSMNode node : ring.nodes) {
                            nodeMap.put(new Coordinate(node.lon, node.lat), node);
                        }
                        for (Ring inner : ring.holes) {
                            for (OSMNode node : inner.nodes) {
                                nodeMap.put(new Coordinate(node.lon, node.lat), node);
                            }
                        }
                    }
                }
                GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();
                Geometry u = geometryFactory.createMultiPolygon(allRings.toArray(new Polygon[allRings.size()]));
                u = u.union();
                
                if (u instanceof GeometryCollection) {
                    GeometryCollection mp = (GeometryCollection) u;
                    for (int i = 0; i < mp.getNumGeometries(); ++i) {
                        Geometry poly = mp.getGeometryN(i);
                        if (!(poly instanceof Polygon)) {
                            LOG.warn("Unexpected non-polygon when merging areas: " + poly);
                            continue;
                        }
                        this.outermostRings.add(toRing((Polygon) poly, nodeMap));
                    }
                } else if (u instanceof Polygon) {
                    this.outermostRings.add(toRing((Polygon) u, nodeMap));
                } else {
                    LOG.warn("Unexpected non-polygon when merging areas: " + u);
                }
            }
            
            public class RingConstructionException extends RuntimeException {
                private static final long serialVersionUID = 1L;
            }
            
            private Ring toRing(Polygon polygon, HashMap<Coordinate, OSMNode> nodeMap) {
                List<OSMNode> shell = new ArrayList<OSMNode>();
                for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
                    OSMNode node = nodeMap.get(coord);
                    if (node == null) { throw new RingConstructionException(); }
                    shell.add(node);
                }
                Ring ring = new Ring(shell, true);
                // now the holes
                for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
                    LineString interior = polygon.getInteriorRingN(i);
                    List<OSMNode> hole = new ArrayList<OSMNode>();
                    for (Coordinate coord : interior.getCoordinates()) {
                        OSMNode node = nodeMap.get(coord);
                        if (node == null) { throw new RingConstructionException(); }
                        hole.add(node);
                    }
                    ring.holes.add(new Ring(hole, true));
                }
                
                return ring;
            }
            
            public OSMWithTags getSomeOSMObject() {
                return this.areas.iterator().next().parent;
            }
        }
        
        private final Graph graph;
        
        /**
         * The bike safety factor of the safest street
         */
        private double bestBikeSafety = 1;
        
        // track OSM nodes which are decomposed into multiple graph vertices because they are
        // elevators. later they will be iterated over to build ElevatorEdges between them.
        private final HashMap<Long, HashMap<OSMLevel, IntersectionVertex>> multiLevelNodes = new HashMap<Long, HashMap<OSMLevel, IntersectionVertex>>();
        
        // track OSM nodes that will become graph vertices because they appear in multiple OSM ways
        private final Map<Long, IntersectionVertex> intersectionNodes = new HashMap<Long, IntersectionVertex>();
        
        // track vertices to be removed in the turn-graph conversion.
        // this is a superset of intersectionNodes.values, which contains
        // a null vertex reference for multilevel nodes. the individual vertices
        // for each level of a multilevel node are includeed in endpoints.
        private final ArrayList<IntersectionVertex> endpoints = new ArrayList<IntersectionVertex>();
        
        // track which vertical level each OSM way belongs to, for building elevators etc.
        private final Map<OSMWithTags, OSMLevel> wayLevels = new HashMap<OSMWithTags, OSMLevel>();
        
        private final HashSet<OSMNode> _bikeRentalNodes = new HashSet<OSMNode>();
        
        private final DistanceLibrary distanceLibrary = SphericalDistanceLibrary.getInstance();
        
        private final HashMap<Coordinate, IntersectionVertex> areaBoundaryVertexForCoordinate = new HashMap<Coordinate, IntersectionVertex>();
        
        public Handler(Graph graph) {
            this.graph = graph;
        }
        
        public void buildGraph(HashMap<Class<?>, Object> extra) {
            
            // handle turn restrictions, road names, and level maps in relations
            processRelations();
            
            if (OpenStreetMapGraphBuilderImpl.this.staticBikeRental) {
                processBikeRentalNodes();
            }
            
            // Remove all simple islands
            HashSet<Long> _keep = new HashSet<Long>(this._nodesWithNeighbors);
            _keep.addAll(this._areaNodes);
            this._nodes.keySet().retainAll(_keep);
            
            // figure out which nodes that are actually intersections
            initIntersectionNodes();
            
            buildBasicGraph();
            if (OpenStreetMapGraphBuilderImpl.this.skipVisibility) {
                LOG.info("Skipping visibility graph construction for walkable areas.");
            } else {
                buildWalkableAreas();
            }
            if (OpenStreetMapGraphBuilderImpl.this.staticParkAndRide) {
                buildParkAndRideAreas();
            }
            
            buildElevatorEdges(this.graph);
            
            /* unify turn restrictions */
            for (Long fromWay : this.turnRestrictionsByFromWay.keySet()) {
                for (TurnRestrictionTag restrictionTag : this.turnRestrictionsByFromWay.get(fromWay)) {
                    if (restrictionTag.possibleFrom.isEmpty()) {
                        LOG.warn("No from edge found for " + restrictionTag);
                        continue;
                    }
                    if (restrictionTag.possibleTo.isEmpty()) {
                        LOG.warn("No to edge found for " + restrictionTag);
                        continue;
                    }
                    for (PlainStreetEdge from : restrictionTag.possibleFrom) {
                        if (from == null) {
                            LOG.warn("from-edge is null in turn " + restrictionTag);
                            continue;
                        }
                        for (PlainStreetEdge to : restrictionTag.possibleTo) {
                            if ((from == null) || (to == null)) {
                                continue;
                            }
                            int angleDiff = from.getOutAngle() - to.getInAngle();
                            if (angleDiff < 0) {
                                angleDiff += 360;
                            }
                            switch (restrictionTag.direction) {
                                case LEFT:
                                    if (angleDiff >= 160) {
                                        continue; // not a left turn
                                    }
                                    break;
                                case RIGHT:
                                    if (angleDiff <= 200) {
                                        continue; // not a right turn
                                    }
                                    break;
                                case U:
                                    if (((angleDiff <= 150) || (angleDiff > 210))) {
                                        continue; // not a U turn
                                    }
                                    break;
                                case STRAIGHT:
                                    if ((angleDiff >= 30) && (angleDiff < 330)) {
                                        continue; // not straight
                                    }
                                    break;
                            }
                            TurnRestriction restriction = new TurnRestriction();
                            restriction.from = from;
                            restriction.to = to;
                            restriction.type = restrictionTag.type;
                            restriction.modes = restrictionTag.modes;
                            restriction.time = restrictionTag.time;
                            from.addTurnRestriction(restriction);
                        }
                    }
                }
            }
            
            if (OpenStreetMapGraphBuilderImpl.this.customNamer != null) {
                OpenStreetMapGraphBuilderImpl.this.customNamer.postprocess(this.graph);
            }
            
            // generate elevation profiles
            extra.put(ElevationPoint.class, OpenStreetMapGraphBuilderImpl.this.elevationData);
            
            applyBikeSafetyFactor(this.graph);
        } // END buildGraph()
        
        private void processBikeRentalNodes() {
            LOG.info("Processing bike rental nodes...");
            int n = 0;
            BikeRentalStationService bikeRentalService = new BikeRentalStationService();
            this.graph.putService(BikeRentalStationService.class, bikeRentalService);
            for (OSMNode node : this._bikeRentalNodes) {
                n++;
                String creativeName = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getCreativeNameForWay(node);
                int capacity = Integer.MAX_VALUE;
                if (node.hasTag("capacity")) {
                    try {
                        capacity = node.getCapacity();
                    } catch (NumberFormatException e) {
                        LOG.warn("Capacity for osm node " + node.getId() + " (" + creativeName + ") is not a number: "
                                + node.getTag("capacity"));
                    }
                }
                String networks = node.getTag("network");
                String operators = node.getTag("operator");
                Set<String> networkSet = new HashSet<String>();
                if (networks != null) {
                    networkSet.addAll(Arrays.asList(networks.split(";")));
                }
                if (operators != null) {
                    networkSet.addAll(Arrays.asList(operators.split(";")));
                }
                if (networkSet.isEmpty()) {
                    LOG.warn("Bike rental station at osm node " + node.getId() + " (" + creativeName
                            + ") with no network; including as compatible-with-all.");
                    networkSet.add("*"); // Special "catch-all" value
                }
                BikeRentalStation station = new BikeRentalStation();
                station.id = "" + node.getId();
                station.name = creativeName;
                station.x = node.lon;
                station.y = node.lat;
                // The following make sure that spaces+bikes=capacity, always.
                // Also, for the degenerate case of capacity=1, we should have 1
                // bike available, not 0.
                station.spacesAvailable = capacity / 2;
                station.bikesAvailable = capacity - station.spacesAvailable;
                station.realTimeData = false;
                bikeRentalService.addStation(station);
                BikeRentalStationVertex stationVertex = new BikeRentalStationVertex(this.graph, station);
                new RentABikeOnEdge(stationVertex, stationVertex, networkSet);
                new RentABikeOffEdge(stationVertex, stationVertex, networkSet);
            }
            LOG.info("Created " + n + " bike rental stations.");
        }
        
        final int MAX_AREA_NODES = 500;
        
        private void buildWalkableAreas() {
            LOG.info("Building visibility graphs for walkable areas.");
            List<AreaGroup> areaGroups = groupAreas(this._walkableAreas);
            for (AreaGroup group : areaGroups) {
                buildWalkableAreasForGroup(group);
            }
            LOG.info("Done building visibility graphs for walkable areas.");
        }
        
        /**
         * Theoretically, it is not correct to build the visibility graph on the joined polygon of
         * areas with different levels of bike safety. That's because in the optimal path, you might
         * end up changing direction at area boundaries. The problem is known as "weighted planar
         * subdivisions", and the best known algorithm is O(N^3). That's not much worse than general
         * visibility graph construction, but it would have to be done at runtime to account for the
         * differences in bike safety preferences. Ted Chiang's "Story Of Your Life" describes how a
         * very similar problem in optics gives rise to Snell's Law. It is the second-best story
         * about a law of physics that I know of (Chiang's "Exhalation" is the first).
         * <p/>
         * Anyway, since we're not going to run an O(N^3) algorithm at runtime just to give people
         * who don't understand Snell's Law weird paths that they can complain about, this should be
         * just fine.
         *
         * @param group
         */
        private void buildWalkableAreasForGroup(AreaGroup group) {
            Set<OSMNode> startingNodes = new HashSet<OSMNode>();
            Set<Vertex> startingVertices = new HashSet<Vertex>();
            Set<Edge> edges = new HashSet<Edge>();
            
            // create polygon and accumulate nodes for area
            for (Ring ring : group.outermostRings) {
                
                AreaEdgeList edgeList = new AreaEdgeList();
                // the points corresponding to concave or hole vertices
                // or those linked to ways
                ArrayList<VLPoint> visibilityPoints = new ArrayList<VLPoint>();
                ArrayList<OSMNode> visibilityNodes = new ArrayList<OSMNode>();
                HashSet<P2<OSMNode>> alreadyAddedEdges = new HashSet<P2<OSMNode>>();
                // we need to accumulate visibility points from all contained areas
                // inside this ring, but only for shared nodes; we don't care about
                // convexity, which we'll handle for the grouped area only.
                
                // we also want to fill in the edges of this area anyway, because we can,
                // and to avoid the numerical problems that they tend to cause
                for (Area area : group.areas) {
                    if (!ring.toJtsPolygon().contains(area.toJTSMultiPolygon())) {
                        continue;
                    }
                    
                    // Add stops from public transit relations into the area
                    if (this.stopsInAreas.containsKey(area.parent)) {
                        for (OSMNode node : this.stopsInAreas.get(area.parent)) {
                            addtoVisibilityAndStartSets(startingNodes, visibilityPoints, visibilityNodes, node);
                        }
                    }
                    
                    for (Ring outerRing : area.outermostRings) {
                        for (int i = 0; i < outerRing.nodes.size(); ++i) {
                            OSMNode node = outerRing.nodes.get(i);
                            createEdgesForRingSegment(edges, edgeList, area, outerRing, i, alreadyAddedEdges);
                            addtoVisibilityAndStartSets(startingNodes, visibilityPoints, visibilityNodes, node);
                        }
                        for (Ring innerRing : outerRing.holes) {
                            for (int j = 0; j < innerRing.nodes.size(); ++j) {
                                OSMNode node = innerRing.nodes.get(j);
                                createEdgesForRingSegment(edges, edgeList, area, innerRing, j, alreadyAddedEdges);
                                addtoVisibilityAndStartSets(startingNodes, visibilityPoints, visibilityNodes, node);
                            }
                        }
                    }
                }
                List<OSMNode> nodes = new ArrayList<OSMNode>();
                List<VLPoint> vertices = new ArrayList<VLPoint>();
                accumulateRingNodes(ring, nodes, vertices);
                VLPolygon polygon = makeStandardizedVLPolygon(vertices, nodes, false);
                accumulateVisibilityPoints(ring.nodes, polygon, visibilityPoints, visibilityNodes, false);
                
                ArrayList<VLPolygon> polygons = new ArrayList<VLPolygon>();
                polygons.add(polygon);
                // holes
                for (Ring innerRing : ring.holes) {
                    ArrayList<OSMNode> holeNodes = new ArrayList<OSMNode>();
                    vertices = new ArrayList<VLPoint>();
                    accumulateRingNodes(innerRing, holeNodes, vertices);
                    VLPolygon hole = makeStandardizedVLPolygon(vertices, holeNodes, true);
                    accumulateVisibilityPoints(innerRing.nodes, hole, visibilityPoints, visibilityNodes, true);
                    nodes.addAll(holeNodes);
                    polygons.add(hole);
                }
                
                Environment areaEnv = new Environment(polygons);
                // FIXME: temporary hard limit on size of
                // areas to prevent way explosion
                if (visibilityPoints.size() > this.MAX_AREA_NODES) {
                    LOG.warn("Area " + group.getSomeOSMObject() + " is too complicated (" + visibilityPoints.size() + " > "
                            + this.MAX_AREA_NODES);
                    continue;
                }
                
                if (!areaEnv.is_valid(VISIBILITY_EPSILON)) {
                    LOG.warn("Area " + group.getSomeOSMObject() + " is not epsilon-valid (epsilon = " + VISIBILITY_EPSILON + ")");
                    continue;
                }
                
                edgeList.setOriginalEdges(ring.toJtsPolygon());
                
                createNamedAreas(edgeList, ring, group.areas);
                
                OSMWithTags areaEntity = group.getSomeOSMObject();
                
                for (int i = 0; i < visibilityNodes.size(); ++i) {
                    OSMNode nodeI = visibilityNodes.get(i);
                    VisibilityPolygon visibilityPolygon = new VisibilityPolygon(visibilityPoints.get(i), areaEnv,
                            VISIBILITY_EPSILON);
                    Polygon poly = toJTSPolygon(visibilityPolygon);
                    for (int j = 0; j < visibilityNodes.size(); ++j) {
                        OSMNode nodeJ = visibilityNodes.get(j);
                        P2<OSMNode> nodePair = new P2<OSMNode>(nodeI, nodeJ);
                        if (alreadyAddedEdges.contains(nodePair)) {
                            continue;
                        }
                        
                        IntersectionVertex startEndpoint = getVertexForOsmNode(nodeI, areaEntity);
                        IntersectionVertex endEndpoint = getVertexForOsmNode(nodeJ, areaEntity);
                        
                        Coordinate[] coordinates = new Coordinate[] { startEndpoint.getCoordinate(), endEndpoint.getCoordinate() };
                        GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();
                        LineString line = geometryFactory.createLineString(coordinates);
                        if (poly.contains(line)) {
                            
                            createSegments(nodeI, nodeJ, startEndpoint, endEndpoint, group.areas, edgeList, edges);
                            if (startingNodes.contains(nodeI)) {
                                startingVertices.add(startEndpoint);
                            }
                            if (startingNodes.contains(nodeJ)) {
                                startingVertices.add(endEndpoint);
                            }
                        }
                    }
                }
            }
            pruneAreaEdges(startingVertices, edges);
        }

        private void buildParkAndRideAreas() {
            LOG.info("Building P+R areas");
            List<AreaGroup> areaGroups = groupAreas(this._parkAndRideAreas);
            int n = 0;
            for (AreaGroup group : areaGroups) {
                if (buildParkAndRideAreasForGroup(group)) {
                    n++;
                }
            }
            LOG.info("Created {} P+R.", n);
        }
        
        private boolean buildParkAndRideAreasForGroup(AreaGroup group) {
            Envelope2D envelope = null;
            // Process all nodes from outer rings
            List<IntersectionVertex> accessVertexes = new ArrayList<IntersectionVertex>();
            String creativeName = null;
            long osmId = 0L;
            for (Area area : group.areas) {
                osmId = area.parent.getId();
                if ((creativeName == null) || (area.parent.getTag("name") != null)) {
                    creativeName = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getCreativeNameForWay(area.parent);
                }
                for (Ring ring : area.outermostRings) {
                    for (OSMNode node : ring.nodes) {
                        // We need to lazy create the envelope as the default
                        // constructor include (0,0) in the bounds...
                        if (envelope == null) {
                            envelope = new Envelope2D(null, node.lon, node.lat, 0, 0);
                        } else {
                            envelope.add(node.lon, node.lat);
                        }
                        IntersectionVertex accessVertex = getVertexForOsmNode(node, area.parent);
                        if (accessVertex.getIncoming().isEmpty() || accessVertex.getOutgoing().isEmpty()) {
                            continue;
                        }
                        accessVertexes.add(accessVertex);
                    }
                }
            }
            // Check P+R accessibility by walking and driving.
            TraversalRequirements walkReq = new TraversalRequirements(new RoutingRequest(TraverseMode.WALK));
            TraversalRequirements driveReq = new TraversalRequirements(new RoutingRequest(TraverseMode.CAR));
            boolean walkAccessibleIn = false;
            boolean carAccessibleIn = false;
            boolean walkAccessibleOut = false;
            boolean carAccessibleOut = false;
            for (IntersectionVertex accessVertex : accessVertexes) {
                for (Edge incoming : accessVertex.getIncoming()) {
                    if (incoming instanceof StreetEdge) {
                        if (walkReq.canBeTraversed((StreetEdge) incoming)) {
                            walkAccessibleIn = true;
                        }
                        if (driveReq.canBeTraversed((StreetEdge) incoming)) {
                            carAccessibleIn = true;
                        }
                    }
                }
                for (Edge outgoing : accessVertex.getOutgoing()) {
                    if (outgoing instanceof StreetEdge) {
                        if (walkReq.canBeTraversed((StreetEdge) outgoing)) {
                            walkAccessibleOut = true;
                        }
                        if (driveReq.canBeTraversed((StreetEdge) outgoing)) {
                            carAccessibleOut = true;
                        }
                    }
                }
            }
            if (walkAccessibleIn != walkAccessibleOut) {
                LOG.error("P+R walk IN/OUT accessibility mismatch! Please have a look as this should not happen.");
            }
            if (!walkAccessibleOut || !carAccessibleIn) {
                // This will prevent the P+R to be useful.
                LOG.warn(this.graph.addBuilderAnnotation(new ParkAndRideUnlinked(creativeName, osmId)));
                return false;
            }
            if (!walkAccessibleIn || !carAccessibleOut) {
                LOG.warn("P+R '{}' ({}) is not walk-accessible");
                // This does not prevent routing as we only use P+R for car dropoff,
                // but this is an issue with OSM data.
            }
            // Place the P+R at the center of the envelope
            ParkAndRideVertex parkAndRideVertex = new ParkAndRideVertex(this.graph, "P+R" + osmId, "P+R_" + osmId, envelope
                    .getCenterX(), envelope.getCenterY(), creativeName);
            new ParkAndRideEdge(parkAndRideVertex);
            for (IntersectionVertex accessVertex : accessVertexes) {
                new ParkAndRideLinkEdge(parkAndRideVertex, accessVertex);
                new ParkAndRideLinkEdge(accessVertex, parkAndRideVertex);
            }
            LOG.debug("Created P+R '{}' ({})", creativeName, osmId);
            return true;
        }
        
        private void addtoVisibilityAndStartSets(Set<OSMNode> startingNodes, ArrayList<VLPoint> visibilityPoints,
                ArrayList<OSMNode> visibilityNodes, OSMNode node) {
            if (this._nodesWithNeighbors.contains(node.getId()) || multipleAreasContain(node.getId()) || node.isStop()) {
                
                startingNodes.add(node);
                VLPoint point = new VLPoint(node.lon, node.lat);
                if (!visibilityPoints.contains(point)) {
                    visibilityPoints.add(point);
                    visibilityNodes.add(node);
                }
            }
        }
        
        private Polygon toJTSPolygon(VLPolygon visibilityPolygon) {
            // incomprehensibly, visilibity's routines for figuring out point-polygon containment
            // are too broken
            // to use here, so we have to fall back to JTS.
            Coordinate[] coordinates = new Coordinate[visibilityPolygon.n() + 1];
            
            for (int p = 0; p < coordinates.length; ++p) {
                VLPoint vlPoint = visibilityPolygon.get(p);
                coordinates[p] = new Coordinate(vlPoint.x, vlPoint.y);
            }
            LinearRing shell = GeometryUtils.getGeometryFactory().createLinearRing(coordinates);
            Polygon poly = GeometryUtils.getGeometryFactory().createPolygon(shell, new LinearRing[0]);
            return poly;
        }
        
        private void createEdgesForRingSegment(Set<Edge> edges, AreaEdgeList edgeList, Area area, Ring ring, int i,
                HashSet<P2<OSMNode>> alreadyAddedEdges) {
            OSMNode node = ring.nodes.get(i);
            OSMNode nextNode = ring.nodes.get((i + 1) % ring.nodes.size());
            P2<OSMNode> nodePair = new P2<OSMNode>(node, nextNode);
            if (alreadyAddedEdges.contains(nodePair)) { return; }
            alreadyAddedEdges.add(nodePair);
            IntersectionVertex startEndpoint = getVertexForOsmNode(node, area.parent);
            IntersectionVertex endEndpoint = getVertexForOsmNode(nextNode, area.parent);
            
            createSegments(node, nextNode, startEndpoint, endEndpoint, Arrays.asList(area), edgeList, edges);
        }
        
        private void createNamedAreas(AreaEdgeList edgeList, Ring ring, Collection<Area> areas) {
            Polygon containingArea = ring.toJtsPolygon();
            for (Area area : areas) {
                Geometry intersection = containingArea.intersection(area.toJTSMultiPolygon());
                if (intersection.getArea() == 0) {
                    continue;
                }
                NamedArea namedArea = new NamedArea();
                OSMWithTags areaEntity = area.parent;
                int cls = StreetEdge.CLASS_OTHERPATH;
                cls |= getStreetClasses(areaEntity);
                namedArea.setStreetClass(cls);
                
                String id = "way (area) " + areaEntity.getId() + " (splitter linking)";
                id = unique(id);
                String name = getNameForWay(areaEntity, id);
                namedArea.setName(name);
                
                WayProperties wayData = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getDataForWay(areaEntity);
                Double safety = wayData.getSafetyFeatures().getFirst();
                namedArea.setBicycleSafetyMultiplier(safety);
                
                namedArea.setOriginalEdges(intersection);
                
                StreetTraversalPermission permission = getPermissionsForEntity(areaEntity,
                        StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE);
                namedArea.setPermission(permission);
                
                edgeList.addArea(namedArea);
            }
        }
        
        private void createSegments(OSMNode fromNode, OSMNode toNode, IntersectionVertex startEndpoint,
                IntersectionVertex endEndpoint, Collection<Area> areas, AreaEdgeList edgeList, Set<Edge> edges) {
            
            List<Area> intersects = new ArrayList<Area>();
            
            Coordinate[] coordinates = new Coordinate[] { startEndpoint.getCoordinate(), endEndpoint.getCoordinate() };
            GeometryFactory geometryFactory = GeometryUtils.getGeometryFactory();
            LineString line = geometryFactory.createLineString(coordinates);
            for (Area area : areas) {
                MultiPolygon polygon = area.toJTSMultiPolygon();
                Geometry intersection = polygon.intersection(line);
                if (intersection.getLength() > 0.000001) {
                    intersects.add(area);
                }
            }
            if (intersects.size() == 0) {
                // apparently our intersection here was bogus
                return;
            }
            // do we need to recurse?
            if (intersects.size() == 1) {
                Area area = intersects.get(0);
                OSMWithTags areaEntity = area.parent;
                
                StreetTraversalPermission areaPermissions = getPermissionsForEntity(areaEntity,
                        StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE);
                
                float carSpeed = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getCarSpeedForWay(areaEntity, false);
                
                double length = this.distanceLibrary.distance(startEndpoint.getCoordinate(), endEndpoint.getCoordinate());
                
                int cls = StreetEdge.CLASS_OTHERPATH;
                cls |= getStreetClasses(areaEntity);
                
                String label = "way (area) " + areaEntity.getId() + " from " + startEndpoint.getLabel() + " to "
                        + endEndpoint.getLabel();
                label = unique(label);
                String name = getNameForWay(areaEntity, label);
                
                AreaEdge street = OpenStreetMapGraphBuilderImpl.this.edgeFactory.createAreaEdge(fromNode, toNode, areaEntity,
                        startEndpoint, endEndpoint, line, name, length, areaPermissions, false, carSpeed, edgeList);
                
                street.setStreetClass(cls);
                edges.add(street);
                
                label = "way (area) " + areaEntity.getId() + " from " + endEndpoint.getLabel() + " to "
                        + startEndpoint.getLabel();
                label = unique(label);
                name = getNameForWay(areaEntity, label);
                
                AreaEdge backStreet = OpenStreetMapGraphBuilderImpl.this.edgeFactory.createAreaEdge(toNode, fromNode, areaEntity,
                        endEndpoint, startEndpoint, (LineString) line.reverse(), name, length, areaPermissions, true, carSpeed,
                        edgeList);
                
                backStreet.setStreetClass(cls);
                edges.add(backStreet);
                
                WayProperties wayData = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getDataForWay(areaEntity);
                applyWayProperties(street, backStreet, wayData, areaEntity);
                
            } else {
                // take the part that intersects with the start vertex
                Coordinate startCoordinate = startEndpoint.getCoordinate();
                Point startPoint = geometryFactory.createPoint(startCoordinate);
                for (Area area : intersects) {
                    MultiPolygon polygon = area.toJTSMultiPolygon();
                    if (!(polygon.intersects(startPoint) || polygon.getBoundary().intersects(startPoint))) {
                        continue;
                    }
                    Geometry lineParts = line.intersection(polygon);
                    if (lineParts.getLength() > 0.000001) {
                        Coordinate edgeCoordinate = null;
                        // this is either a LineString or a MultiLineString (we hope)
                        if (lineParts instanceof MultiLineString) {
                            MultiLineString mls = (MultiLineString) lineParts;
                            boolean found = false;
                            for (int i = 0; i < mls.getNumGeometries(); ++i) {
                                LineString segment = (LineString) mls.getGeometryN(i);
                                if (found) {
                                    edgeCoordinate = segment.getEndPoint().getCoordinate();
                                    break;
                                }
                                if (segment.contains(startPoint) || segment.getBoundary().contains(startPoint)) {
                                    found = true;
                                    if (segment.getLength() > 0.000001) {
                                        edgeCoordinate = segment.getEndPoint().getCoordinate();
                                        break;
                                    }
                                }
                            }
                        } else if (lineParts instanceof LineString) {
                            edgeCoordinate = ((LineString) lineParts).getEndPoint().getCoordinate();
                        } else {
                            continue;
                        }
                        
                        IntersectionVertex newEndpoint = this.areaBoundaryVertexForCoordinate.get(edgeCoordinate);
                        if (newEndpoint == null) {
                            newEndpoint = new IntersectionVertex(this.graph, "area splitter at " + edgeCoordinate,
                                    edgeCoordinate.x, edgeCoordinate.y);
                            this.areaBoundaryVertexForCoordinate.put(edgeCoordinate, newEndpoint);
                        }
                        createSegments(fromNode, toNode, startEndpoint, newEndpoint, Arrays.asList(area), edgeList, edges);
                        createSegments(fromNode, toNode, newEndpoint, endEndpoint, intersects, edgeList, edges);
                        break;
                    }
                }
            }
        }
        
        private void accumulateRingNodes(Ring ring, List<OSMNode> nodes, List<VLPoint> vertices) {
            for (OSMNode node : ring.nodes) {
                if (nodes.contains(node)) {
                    // hopefully, this only happens in order to
                    // close polygons
                    continue;
                }
                VLPoint point = new VLPoint(node.lon, node.lat);
                nodes.add(node);
                vertices.add(point);
            }
        }
        
        private void accumulateVisibilityPoints(List<OSMNode> nodes, VLPolygon polygon, List<VLPoint> visibilityPoints,
                List<OSMNode> visibilityNodes, boolean hole) {
            int n = polygon.vertices.size();
            for (int i = 0; i < n; ++i) {
                OSMNode curNode = nodes.get(i);
                VLPoint cur = polygon.vertices.get(i);
                VLPoint prev = polygon.vertices.get(((i + n) - 1) % n);
                VLPoint next = polygon.vertices.get((i + 1) % n);
                if (hole || ((((cur.x - prev.x) * (next.y - cur.y)) - ((cur.y - prev.y) * (next.x - cur.x))) > 0)) {
                    // that math up there is a cross product to check
                    // if the point is concave. Note that the sign is reversed because
                    // visilibity is either ccw or latitude-major
                    
                    if (!visibilityNodes.contains(curNode)) {
                        visibilityPoints.add(cur);
                        visibilityNodes.add(curNode);
                    }
                }
            }
        }
        
        private VLPolygon makeStandardizedVLPolygon(List<VLPoint> vertices, List<OSMNode> nodes, boolean reversed) {
            VLPolygon polygon = new VLPolygon(vertices);
            
            if ((reversed && (polygon.area() > 0)) || (!reversed && (polygon.area() < 0))) {
                polygon.reverse();
                // need to reverse nodes as well
                reversePolygonOfOSMNodes(nodes);
            }
            
            if (!polygon.is_in_standard_form()) {
                standardize(polygon.vertices, nodes);
            }
            return polygon;
        }
        
        private List<AreaGroup> groupAreas(List<Area> areas) {
            DisjointSet<Area> groups = new DisjointSet<Area>();
            Multimap<OSMNode, Area> areasForNode = LinkedListMultimap.create();
            for (Area area : areas) {
                for (Ring ring : area.outermostRings) {
                    for (Ring inner : ring.holes) {
                        for (OSMNode node : inner.nodes) {
                            areasForNode.put(node, area);
                        }
                    }
                    for (OSMNode node : ring.nodes) {
                        areasForNode.put(node, area);
                    }
                }
            }
            
            // areas that can be joined must share nodes and levels
            for (OSMNode osmNode : areasForNode.keySet()) {
                for (Area area1 : areasForNode.get(osmNode)) {
                    OSMLevel level1 = this.wayLevels.get(area1.parent);
                    for (Area area2 : areasForNode.get(osmNode)) {
                        OSMLevel level2 = this.wayLevels.get(area2.parent);
                        if (((level1 == null) && (level2 == null)) || ((level1 != null) && level1.equals(level2))) {
                            groups.union(area1, area2);
                        }
                    }
                }
            }
            
            List<AreaGroup> out = new ArrayList<AreaGroup>();
            for (Set<Area> areaSet : groups.sets()) {
                try {
                    out.add(new AreaGroup(areaSet));
                } catch (AreaGroup.RingConstructionException e) {
                    for (Area area : areaSet) {
                        LOG.debug("Failed to create merged area for " + area
                                + ".  This area might not be at fault; it might be one of the other areas in this list.");
                        out.add(new AreaGroup(Arrays.asList(area)));
                    }
                }
            }
            return out;
        }
        
        private void standardize(ArrayList<VLPoint> vertices, List<OSMNode> nodes) {
            // based on code from VisiLibity
            int point_count = vertices.size();
            if (point_count > 1) { // if more than one point in the polygon.
                ArrayList<VLPoint> vertices_temp = new ArrayList<VLPoint>(point_count);
                ArrayList<OSMNode> nodes_temp = new ArrayList<OSMNode>(point_count);
                // Find index of lexicographically smallest point.
                int index_of_smallest = 0;
                for (int i = 1; i < point_count; i++) {
                    if (vertices.get(i).compareTo(vertices.get(index_of_smallest)) < 0) {
                        index_of_smallest = i;
                    }
                }
                // minor optimization for already-standardized polygons
                if (index_of_smallest == 0) { return; }
                // Fill vertices_temp starting with lex. smallest.
                for (int i = index_of_smallest; i < point_count; i++) {
                    vertices_temp.add(vertices.get(i));
                    nodes_temp.add(nodes.get(i));
                }
                for (int i = 0; i < index_of_smallest; i++) {
                    vertices_temp.add(vertices.get(i));
                    nodes_temp.add(nodes.get(i));
                }
                for (int i = 0; i < point_count; ++i) {
                    vertices.set(i, vertices_temp.get(i));
                    nodes.set(i, nodes_temp.get(i));
                }
            }
        }
        
        private boolean multipleAreasContain(long id) {
            Set<OSMWay> areas = this._areasForNode.get(id);
            if (areas == null) { return false; }
            return areas.size() > 1;
        }
        
        class ListedEdgesOnly implements SkipEdgeStrategy {
            private final Set<Edge> edges;
            
            public ListedEdgesOnly(Set<Edge> edges) {
                this.edges = edges;
            }
            
            @Override
            public boolean shouldSkipEdge(Vertex origin, Vertex target, State current, Edge edge, ShortestPathTree spt,
                    RoutingRequest traverseOptions) {
                return !this.edges.contains(edge);
            }
            
        }
        
        /**
         * Do an all-pairs shortest path search from a list of vertices over a specified set of
         * edges, and retain only those edges which are actually used in some shortest path.
         *
         * @param startingVertices
         * @param edges
         */
        private void pruneAreaEdges(Collection<Vertex> startingVertices, Set<Edge> edges) {
            if (edges.size() == 0) { return; }
            TraverseMode mode;
            PlainStreetEdge firstEdge = (PlainStreetEdge) edges.iterator().next();
            
            if (firstEdge.getPermission().allows(StreetTraversalPermission.PEDESTRIAN)) {
                mode = TraverseMode.WALK;
            } else if (firstEdge.getPermission().allows(StreetTraversalPermission.BICYCLE)) {
                mode = TraverseMode.BICYCLE;
            } else {
                mode = TraverseMode.CAR;
            }
            RoutingRequest options = new RoutingRequest(mode);
            GenericDijkstra search = new GenericDijkstra(options);
            search.setSkipEdgeStrategy(new ListedEdgesOnly(edges));
            Set<Edge> usedEdges = new HashSet<Edge>();
            for (Vertex vertex : startingVertices) {
                State state = new State(vertex, options);
                ShortestPathTree spt = search.getShortestPathTree(state);
                for (Vertex endVertex : startingVertices) {
                    GraphPath path = spt.getPath(endVertex, false);
                    if (path != null) {
                        for (Edge edge : path.edges) {
                            usedEdges.add(edge);
                        }
                    }
                }
            }
            for (Edge edge : edges) {
                if (!usedEdges.contains(edge)) {
                    edge.detach();
                }
            }
        }
        
        private void reversePolygonOfOSMNodes(List<OSMNode> nodes) {
            for (int i = 1; i < ((nodes.size() + 1) / 2); ++i) {
                OSMNode tmp = nodes.get(i);
                int opposite = nodes.size() - i;
                nodes.set(i, nodes.get(opposite));
                nodes.set(opposite, tmp);
            }
        }
        
        private void buildBasicGraph() {
            
            /* build the street segment graph from OSM ways */
            long wayIndex = 0;
            
            WAY: for (OSMWay way : this._ways.values()) {
                
                if ((wayIndex % 10000) == 0) {
                        LOG.debug("ways=" + wayIndex + "/" + this._ways.size());
                    }
                    wayIndex++;
                
                WayProperties wayData = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getDataForWay(way);
                
                setWayName(way);
                
                StreetTraversalPermission permissions = getPermissionsForWay(way, wayData.getPermission());
                    if (!isWayRoutable(way) || permissions.allowsNothing()) {
                        continue;
                    }
                
                // handle duplicate nodes in OSM ways
                    // this is a workaround for crappy OSM data quality
                    ArrayList<Long> nodes = new ArrayList<Long>(way.getNodeRefs().size());
                    long last = -1;
                    double lastLat = -1, lastLon = -1;
                    String lastLevel = null;
                    for (long nodeId : way.getNodeRefs()) {
                        OSMNode node = this._nodes.get(nodeId);
                        if (node == null) {
                            continue WAY;
                        }
                        boolean levelsDiffer = false;
                        String level = node.getTag("level");
                        if (lastLevel == null) {
                            if (level != null) {
                                levelsDiffer = true;
                            }
                        } else {
                            if (!lastLevel.equals(level)) {
                                levelsDiffer = true;
                            }
                        }
                        if ((nodeId != last) && ((node.lat != lastLat) || (node.lon != lastLon) || levelsDiffer)) {
                            nodes.add(nodeId);
                        }
                        last = nodeId;
                        lastLon = node.lon;
                        lastLat = node.lat;
                        lastLevel = level;
                    }
                
                IntersectionVertex startEndpoint = null, endEndpoint = null;
                
                ArrayList<Coordinate> segmentCoordinates = new ArrayList<Coordinate>();
                
                getLevelsForWay(way);
                
                /*
                 * Traverse through all the nodes of this edge. For nodes which are not shared with
                 * any other edge, do not create endpoints -- just accumulate them for geometry and
                 * ele tags. For nodes which are shared, create endpoints and StreetVertex
                 * instances. One exception: if the next vertex also appears earlier in the way, we
                 * need to split the way, because otherwise we have a way that loops from a vertex
                 * to itself, which could cause issues with splitting.
                 */
                    Long startNode = null;
                    // where the current edge should start
                    OSMNode osmStartNode = null;
                
                for (int i = 0; i < (nodes.size() - 1); i++) {
                        OSMNode segmentStartOSMNode = this._nodes.get(nodes.get(i));
                        if (segmentStartOSMNode == null) {
                            continue;
                        }
                        Long endNode = nodes.get(i + 1);
                        if (osmStartNode == null) {
                            startNode = nodes.get(i);
                            osmStartNode = segmentStartOSMNode;
                        }
                        // where the current edge might end
                        OSMNode osmEndNode = this._nodes.get(endNode);
                    
                    if ((osmStartNode == null) || (osmEndNode == null)) {
                            continue;
                        }
                    
                    LineString geometry;
                    
                    /*
                     * We split segments at intersections, self-intersections, nodes with ele tags,
                     * and transit stops; the only processing we do on other nodes is to accumulate
                     * their geometry
                     */
                        if (segmentCoordinates.size() == 0) {
                            segmentCoordinates.add(getCoordinate(osmStartNode));
                        }
                    
                    if (this.intersectionNodes.containsKey(endNode) || (i == (nodes.size() - 2))
                                || nodes.subList(0, i).contains(nodes.get(i)) || osmEndNode.hasTag("ele") || osmEndNode.isStop()) {
                            segmentCoordinates.add(getCoordinate(osmEndNode));
                        
                        geometry = GeometryUtils.getGeometryFactory().createLineString(
                                    segmentCoordinates.toArray(new Coordinate[0]));
                            segmentCoordinates.clear();
                        } else {
                            segmentCoordinates.add(getCoordinate(osmEndNode));
                            continue;
                        }
                    
                    /* generate endpoints */
                        if (startEndpoint == null) { // first iteration on this way
                            // make or get a shared vertex for flat intersections,
                            // one vertex per level for multilevel nodes like elevators
                            startEndpoint = getVertexForOsmNode(osmStartNode, way);
                            String ele = segmentStartOSMNode.getTag("ele");
                            if (ele != null) {
                                Double elevation = ElevationUtils.parseEleTag(ele);
                                if (elevation != null) {
                                    OpenStreetMapGraphBuilderImpl.this.elevationData.put(startEndpoint, elevation);
                                }
                            }
                        } else { // subsequent iterations
                            startEndpoint = endEndpoint;
                        }
                    
                    endEndpoint = getVertexForOsmNode(osmEndNode, way);
                        String ele = osmEndNode.getTag("ele");
                        if (ele != null) {
                            Double elevation = ElevationUtils.parseEleTag(ele);
                            if (elevation != null) {
                                OpenStreetMapGraphBuilderImpl.this.elevationData.put(endEndpoint, elevation);
                            }
                        }
                        P2<PlainStreetEdge> streets = getEdgesForStreet(startEndpoint, endEndpoint, way, i, osmStartNode.getId(),
                            osmEndNode.getId(), permissions, geometry);
                    
                    PlainStreetEdge street = streets.getFirst();
                        PlainStreetEdge backStreet = streets.getSecond();
                        applyWayProperties(street, backStreet, wayData, way);
                    
                    applyEdgesToTurnRestrictions(way, startNode, endNode, street, backStreet);
                        startNode = endNode;
                        osmStartNode = this._nodes.get(startNode);
                    }
                } // END loop over OSM ways
        }
        
        private void applyWayProperties(PlainStreetEdge street, PlainStreetEdge backStreet, WayProperties wayData, OSMWithTags way) {
            
            Set<Alert> note = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getNoteForWay(way);
            Set<Alert> wheelchairNote = getWheelchairNotes(way);
            boolean noThruTraffic = way.isThroughTrafficExplicitlyDisallowed();
            
            if (street != null) {
                double safety = wayData.getSafetyFeatures().getFirst();
                street.setBicycleSafetyEffectiveLength(street.getLength() * safety);
                if (safety < this.bestBikeSafety) {
                    this.bestBikeSafety = safety;
                }
                if (note != null) {
                    street.setNote(note);
                }
                if (wheelchairNote != null) {
                    street.setWheelchairNote(wheelchairNote);
                }
                street.setNoThruTraffic(noThruTraffic);
            }
            
            if (backStreet != null) {
                double safety = wayData.getSafetyFeatures().getSecond();
                if (safety < this.bestBikeSafety) {
                    this.bestBikeSafety = safety;
                }
                backStreet.setBicycleSafetyEffectiveLength(backStreet.getLength() * safety);
                if (note != null) {
                    backStreet.setNote(note);
                }
                if (wheelchairNote != null) {
                    backStreet.setWheelchairNote(wheelchairNote);
                }
                backStreet.setNoThruTraffic(noThruTraffic);
            }
        }
        
        private void setWayName(OSMWithTags way) {
            if (!way.hasTag("name")) {
                String creativeName = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getCreativeNameForWay(way);
                if (creativeName != null) {
                    way.addTag("otp:gen_name", creativeName);
                }
            }
        }
        
        private void buildElevatorEdges(Graph graph) {
            /* build elevator edges */
            for (Long nodeId : this.multiLevelNodes.keySet()) {
                OSMNode node = this._nodes.get(nodeId);
                // this allows skipping levels, e.g., an elevator that stops
                // at floor 0, 2, 3, and 5.
                // Converting to an Array allows us to
                // subscript it so we can loop over it in twos. Assumedly, it will stay
                // sorted when we convert it to an Array.
                // The objects are Integers, but toArray returns Object[]
                HashMap<OSMLevel, IntersectionVertex> vertices = this.multiLevelNodes.get(nodeId);
                
                /*
                 * first, build FreeEdges to disconnect from the graph, GenericVertices to serve as
                 * attachment points, and ElevatorBoard and ElevatorAlight edges to connect future
                 * ElevatorHop edges to. After this iteration, graph will look like (side view):
                 * +==+~~X +==+~~X +==+~~X + GenericVertex, X EndpointVertex, ~~ FreeEdge, ==
                 * ElevatorBoardEdge/ElevatorAlightEdge Another loop will fill in the
                 * ElevatorHopEdges.
                 */
                
                OSMLevel[] levels = vertices.keySet().toArray(new OSMLevel[0]);
                Arrays.sort(levels);
                ArrayList<Vertex> onboardVertices = new ArrayList<Vertex>();
                for (OSMLevel level : levels) {
                    // get the node to build the elevator out from
                    IntersectionVertex sourceVertex = vertices.get(level);
                    String sourceVertexLabel = sourceVertex.getLabel();
                    String levelName = level.longName;
                    
                    ElevatorOffboardVertex offboardVertex = new ElevatorOffboardVertex(graph, sourceVertexLabel + "_offboard",
                            sourceVertex.getX(), sourceVertex.getY(), levelName);
                    
                    new FreeEdge(sourceVertex, offboardVertex);
                    new FreeEdge(offboardVertex, sourceVertex);
                    
                    ElevatorOnboardVertex onboardVertex = new ElevatorOnboardVertex(graph, sourceVertexLabel + "_onboard",
                            sourceVertex.getX(), sourceVertex.getY(), levelName);
                    
                    new ElevatorBoardEdge(offboardVertex, onboardVertex);
                    new ElevatorAlightEdge(onboardVertex, offboardVertex, level.longName);
                    
                    // accumulate onboard vertices to so they can be connected by hop edges later
                    onboardVertices.add(onboardVertex);
                }
                
                // -1 because we loop over onboardVertices two at a time
                for (Integer i = 0, vSize = onboardVertices.size() - 1; i < vSize; i++) {
                    Vertex from = onboardVertices.get(i);
                    Vertex to = onboardVertices.get(i + 1);
                    
                    // default permissions: pedestrian, wheelchair, and bicycle
                    boolean wheelchairAccessible = true;
                    StreetTraversalPermission permission = StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE;
                    // check for bicycle=no, otherwise assume it's OK to take a bike
                    if (node.isTagFalse("bicycle")) {
                        permission = StreetTraversalPermission.PEDESTRIAN;
                    }
                    // check for wheelchair=no
                    if (node.isTagFalse("wheelchair")) {
                        wheelchairAccessible = false;
                    }
                    
                    // The narrative won't be strictly correct, as it will show the elevator as part
                    // of the cycling leg, but I think most cyclists will figure out that they
                    // should really dismount.
                    ElevatorHopEdge foreEdge = new ElevatorHopEdge(from, to, permission);
                    ElevatorHopEdge backEdge = new ElevatorHopEdge(to, from, permission);
                    foreEdge.wheelchairAccessible = wheelchairAccessible;
                    backEdge.wheelchairAccessible = wheelchairAccessible;
                }
            } // END elevator edge loop
        }
        
        private void applyEdgesToTurnRestrictions(OSMWay way, long startNode, long endNode, PlainStreetEdge street,
                PlainStreetEdge backStreet) {
            /* Check if there are turn restrictions starting on this segment */
            Collection<TurnRestrictionTag> restrictionTags = this.turnRestrictionsByFromWay.get(way.getId());
            
            if (restrictionTags != null) {
                for (TurnRestrictionTag tag : restrictionTags) {
                    if (tag.via == startNode) {
                        tag.possibleFrom.add(backStreet);
                    } else if (tag.via == endNode) {
                        tag.possibleFrom.add(street);
                    }
                }
            }
            
            restrictionTags = this.turnRestrictionsByToWay.get(way.getId());
            if (restrictionTags != null) {
                for (TurnRestrictionTag tag : restrictionTags) {
                    if (tag.via == startNode) {
                        tag.possibleTo.add(street);
                    } else if (tag.via == endNode) {
                        tag.possibleTo.add(backStreet);
                    }
                }
            }
        }
        
        private void getLevelsForWay(OSMWithTags way) {
            /* Determine OSM level for each way, if it was not already set */
            if (!this.wayLevels.containsKey(way)) {
                // if this way is not a key in the wayLevels map, a level map was not
                // already applied in processRelations
                
                /* try to find a level name in tags */
                String levelName = null;
                OSMLevel level = OSMLevel.DEFAULT;
                if (way.hasTag("level")) { // TODO: floating-point levels &c.
                    levelName = way.getTag("level");
                    level = OSMLevel.fromString(levelName, OSMLevel.Source.LEVEL_TAG,
                            OpenStreetMapGraphBuilderImpl.this.noZeroLevels);
                } else if (way.hasTag("layer")) {
                    levelName = way.getTag("layer");
                    level = OSMLevel.fromString(levelName, OSMLevel.Source.LAYER_TAG,
                            OpenStreetMapGraphBuilderImpl.this.noZeroLevels);
                }
                if ((level == null) || (!level.reliable)) {
                    LOG.warn(this.graph.addBuilderAnnotation(new LevelAmbiguous(levelName, way.getId())));
                    level = OSMLevel.DEFAULT;
                }
                this.wayLevels.put(way, level);
            }
        }
        
        private void initIntersectionNodes() {
            Set<Long> possibleIntersectionNodes = new HashSet<Long>();
            for (OSMWay way : this._ways.values()) {
                List<Long> nodes = way.getNodeRefs();
                for (long node : nodes) {
                    if (possibleIntersectionNodes.contains(node)) {
                        this.intersectionNodes.put(node, null);
                    } else {
                        possibleIntersectionNodes.add(node);
                    }
                }
            }
            // Intersect ways at walkable area boundaries if needed.
            for (Area area : this._walkableAreas) {
                for (Ring outerRing : area.outermostRings) {
                    for (OSMNode node : outerRing.nodes) {
                        long nodeId = node.getId();
                        if (possibleIntersectionNodes.contains(nodeId)) {
                            this.intersectionNodes.put(nodeId, null);
                        } else {
                            possibleIntersectionNodes.add(nodeId);
                        }
                    }
                }
            }
            // Intersect ways at P+R area boundaries if needed.
            for (Area area : this._parkAndRideAreas) {
                for (Ring outerRing : area.outermostRings) {
                    for (OSMNode node : outerRing.nodes) {
                        long nodeId = node.getId();
                        if (possibleIntersectionNodes.contains(nodeId)) {
                            this.intersectionNodes.put(nodeId, null);
                        } else {
                            possibleIntersectionNodes.add(nodeId);
                        }
                    }
                }
            }
        }
        
        private Set<Alert> getWheelchairNotes(OSMWithTags way) {
            Map<String, String> tags = way.getTagsByPrefix("wheelchair:description");
            if (tags == null) { return null; }
            Set<Alert> alerts = new HashSet<Alert>();
            Alert alert = new Alert();
            alerts.add(alert);
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (k.equals("wheelchair:description")) {
                    // no language, assume default from TranslatedString
                    alert.alertHeaderText = new TranslatedString(v);
                } else {
                    String lang = k.substring("wheelchair:description:".length());
                    alert.alertHeaderText = new TranslatedString(lang, v);
                }
            }
            return alerts;
        }
        
        /**
         * The safest bike lane should have a safety weight no lower than the time weight of a flat
         * street. This method divides the safety lengths by the length ratio of the safest street,
         * ensuring this property.
         *
         * @param graph
         */
        private void applyBikeSafetyFactor(Graph graph) {
            LOG.info(graph.addBuilderAnnotation(new Graphwide("Multiplying all bike safety values by "
                    + (1 / this.bestBikeSafety))));
            HashSet<Edge> seenEdges = new HashSet<Edge>();
            HashSet<AreaEdgeList> seenAreas = new HashSet<AreaEdgeList>();
            for (Vertex vertex : graph.getVertices()) {
                for (Edge e : vertex.getOutgoing()) {
                    if (e instanceof AreaEdge) {
                        AreaEdgeList areaEdgeList = ((AreaEdge) e).getArea();
                        if (seenAreas.contains(areaEdgeList)) {
                            continue;
                        }
                        seenAreas.add(areaEdgeList);
                        for (NamedArea area : areaEdgeList.getAreas()) {
                            area.setBicycleSafetyMultiplier(area.getBicycleSafetyMultiplier() / this.bestBikeSafety);
                        }
                    }
                    if (!(e instanceof PlainStreetEdge)) {
                        continue;
                    }
                    PlainStreetEdge pse = (PlainStreetEdge) e;
                    
                    if (!seenEdges.contains(e)) {
                        seenEdges.add(e);
                        pse.setBicycleSafetyEffectiveLength(pse.getBicycleSafetyEffectiveLength() / this.bestBikeSafety);
                    }
                }
                for (Edge e : vertex.getIncoming()) {
                    if (!(e instanceof PlainStreetEdge)) {
                        continue;
                    }
                    PlainStreetEdge pse = (PlainStreetEdge) e;
                    
                    if (!seenEdges.contains(e)) {
                        seenEdges.add(e);
                        pse.setBicycleSafetyEffectiveLength(pse.getBicycleSafetyEffectiveLength() / this.bestBikeSafety);
                    }
                }
            }
        }
        
        private Coordinate getCoordinate(OSMNode osmNode) {
            return new Coordinate(osmNode.lon, osmNode.lat);
        }
        
        @Override
        public void addNode(OSMNode node) {
            if (node.isTag("amenity", "bicycle_rental")) {
                this._bikeRentalNodes.add(node);
                return;
            }
            if (!(this._nodesWithNeighbors.contains(node.getId()) || this._areaNodes.contains(node.getId()) || node.isStop())) { return; }
            
            if (this._nodes.containsKey(node.getId())) { return; }
            
            this._nodes.put(node.getId(), node);
            
            if ((this._nodes.size() % 100000) == 0) {
                LOG.debug("nodes=" + this._nodes.size());
            }
        }
        
        @Override
        public void addWay(OSMWay way) {
            /* only add ways once */
            long wayId = way.getId();
            if (this._ways.containsKey(wayId) || this._areaWaysById.containsKey(wayId)) { return; }
            
            if (this._areaWayIds.contains(wayId)) {
                this._areaWaysById.put(wayId, way);
            }
            
            /* filter out ways that are not relevant for routing */
            if (!(isWayRoutable(way) || way.isParkAndRide())) { return; }
            /* An area can be specified as such, or be one by default as an amenity */
            if ((way.isTag("area", "yes") || way.isTag("amenity", "parking")) && (way.getNodeRefs().size() > 2)) {
                // this is an area that's a simple polygon. So we can just add it straight
                // to the areas, if it's not part of a relation.
                if (!this._areaWayIds.contains(wayId)) {
                    this._singleWayAreas.add(way);
                    this._areaWaysById.put(wayId, way);
                    this._areaWayIds.add(wayId);
                    for (Long node : way.getNodeRefs()) {
                        MapUtils.addToMapSet(this._areasForNode, node, way);
                    }
                    getLevelsForWay(way);
                }
                return;
            }
            
            this._ways.put(wayId, way);
            
            if ((this._ways.size() % 10000) == 0) {
                LOG.debug("ways=" + this._ways.size());
            }
        }
        
        /**
         * Determine whether any mode can or should ever traverse the given way. If not, we leave
         * the way out of the OTP graph. Potentially routable ways are those that have the tags :
         * highway=* public_transport=platform railway=platform But not conveyers, proposed
         * highways/roads or those still under construction, and raceways (as well as ways where all
         * access is specifically forbidden to the public).
         * http://wiki.openstreetmap.org/wiki/Tag:highway%3Dproposed
         */
        private boolean isWayRoutable(OSMWithTags way) {
            if (!isOsmEntityRoutable(way)) { return false; }
            
            String highway = way.getTag("highway");
            if ((highway != null)
                    && (highway.equals("conveyer") || highway.equals("proposed") || highway.equals("construction")
                            || highway.equals("raceway") || highway.equals("unbuilt"))) { return false; }
            
            if (way.isGeneralAccessDenied()) {
                // There are exceptions.
                return (way.isMotorcarExplicitlyAllowed() || way.isBicycleExplicitlyAllowed() || way
                        .isPedestrianExplicitlyAllowed());
            }
            
            return true;
        }
        
        @Override
        public void addRelation(OSMRelation relation) {
            if (this._relations.containsKey(relation.getId())) { return; }
            
            if (relation.isTag("type", "multipolygon") && isOsmEntityRoutable(relation)) {
                // OSM MultiPolygons are ferociously complicated, and in fact cannot be processed
                // without reference to the ways that compose them. Accordingly, we will merely
                // mark the ways for preservation here, and deal with the details once we have
                // the ways loaded.
                if (!isWayRoutable(relation) && !relation.isParkAndRide()) { return; }
                for (OSMRelationMember member : relation.getMembers()) {
                    this._areaWayIds.add(member.getRef());
                }
                getLevelsForWay(relation);
            } else if (!(relation.isTag("type", "restriction"))
                    && !(relation.isTag("type", "route") && relation.isTag("route", "road"))
                    && !(relation.isTag("type", "multipolygon") && isOsmEntityRoutable(relation))
                    && !(relation.isTag("type", "level_map"))
                    && !(relation.isTag("type", "public_transport") && relation.isTag("public_transport", "stop_area"))) { return; }
            
            this._relations.put(relation.getId(), relation);
            
            if ((this._relations.size() % 100) == 0) {
                LOG.debug("relations=" + this._relations.size());
            }
            
        }
        
        /**
         * Determines whether this OSM way is considered routable. The majority of routable ways are
         * those with a highway= tag (which includes everything from motorways to hiking trails).
         * Anything with a public_transport=platform or railway=platform tag is also considered
         * routable even if it doesn't have a highway tag. Platforms are however filtered out if
         * they are marked usage=tourism. This prevents miniature tourist railways like the one in
         * Portland's Zoo from receiving a better score and pulling search endpoints away from real
         * transit stops.
         */
        private boolean isOsmEntityRoutable(OSMWithTags osmEntity) {
            if (osmEntity.hasTag("highway")) { return true; }
            if (osmEntity.isTag("public_transport", "platform") || osmEntity.isTag("railway", "platform")) { return !("tourism"
                    .equals(osmEntity.getTag("usage"))); }
            return false;
        }
        
        private String getNodeLabel(OSMNode node) {
            return String.format(nodeLabelFormat, node.getId());
        }
        
        private String getLevelNodeLabel(OSMNode node, OSMLevel level) {
            return String.format(levelnodeLabelFormat, node.getId(), level.shortName);
        }
        
        @Override
        public void secondPhase() {
            // This copies relevant tags to the ways (highway=*) where it doesn't exist, so that
            // the way purging keeps the needed way around.
            // Multipolygons may be processed more than once, which may be needed since
            // some member might be in different files for the same multipolygon.
            
            // NOTE (AMB): this purging phase may not be necessary if highway tags are not
            // copied over from multipolygon relations. Perhaps we can get by with
            // only 2 steps -- ways+relations, followed by used nodes.
            // Ways can be tag-filtered in phase 1.
            
            markNodesForKeeping(this._ways.values(), this._nodesWithNeighbors);
            markNodesForKeeping(this._areaWaysById.values(), this._areaNodes);
        }
        
        /**
         * After all relations, ways, and nodes are loaded, handle areas.
         */
        @Override
        public void nodesLoaded() {
            processMultipolygonRelations();
            AREA: for (OSMWay way : this._singleWayAreas) {
                    if (this._processedAreas.contains(way)) {
                        continue;
                    }
                    for (Long nodeRef : way.getNodeRefs()) {
                        if (!this._nodes.containsKey(nodeRef)) {
                            continue AREA;
                        }
                    }
                    try {
                        newArea(new Area(way, Arrays.asList(way), Collections.<OSMWay> emptyList()));
                    } catch (Area.AreaConstructionException e) {
                        // this area cannot be constructed, but we already have all the
                        // necessary nodes to construct it. So, something must be wrong with
                        // the area; we'll mark it as processed so that we don't retry.
                    }
                    this._processedAreas.add(way);
                }
            
        }
        
        private void markNodesForKeeping(Collection<OSMWay> osmWays, Set<Long> nodeSet) {
            for (Iterator<OSMWay> it = osmWays.iterator(); it.hasNext();) {
                OSMWay way = it.next();
                // Since the way is kept, update nodes-with-neighbors
                List<Long> nodes = way.getNodeRefs();
                if (nodes.size() > 1) {
                    nodeSet.addAll(nodes);
                }
            }
        }
        
        /**
         * Copies useful metadata from multipolygon relations to the relevant ways, or to the area
         * map. This is done at a different time than processRelations(), so that way purging
         * doesn't remove the used ways.
         */
        private void processMultipolygonRelations() {
            RELATION: for (OSMRelation relation : this._relations.values()) {
                    if (this._processedAreas.contains(relation)) {
                        continue;
                    }
                    if (!(relation.isTag("type", "multipolygon") && (isOsmEntityRoutable(relation) || relation.isParkAndRide()))) {
                        continue;
                    }
                    // Area multipolygons -- pedestrian plazas
                    ArrayList<OSMWay> innerWays = new ArrayList<OSMWay>();
                    ArrayList<OSMWay> outerWays = new ArrayList<OSMWay>();
                    for (OSMRelationMember member : relation.getMembers()) {
                        String role = member.getRole();
                        OSMWay way = this._areaWaysById.get(member.getRef());
                        if (way == null) {
                            // relation includes way which does not exist in the data. Skip.
                            continue RELATION;
                        }
                        for (Long nodeId : way.getNodeRefs()) {
                            if (!this._nodes.containsKey(nodeId)) {
                                // this area is missing some nodes, perhaps because it is on
                                // the edge of the region, so we will simply not route on it.
                                continue RELATION;
                            }
                            MapUtils.addToMapSet(this._areasForNode, nodeId, way);
                        }
                        if (role.equals("inner")) {
                            innerWays.add(way);
                        } else if (role.equals("outer")) {
                            outerWays.add(way);
                        } else {
                            LOG.warn("Unexpected role " + role + " in multipolygon");
                        }
                    }
                    this._processedAreas.add(relation);
                    try {
                        newArea(new Area(relation, outerWays, innerWays));
                    } catch (Area.AreaConstructionException e) {
                        continue;
                    }
                
                for (OSMRelationMember member : relation.getMembers()) {
                        // multipolygons for attribute mapping
                        if (!("way".equals(member.getType()) && this._ways.containsKey(member.getRef()))) {
                            continue;
                        }
                    
                    OSMWithTags way = this._ways.get(member.getRef());
                        if (way == null) {
                            continue;
                        }
                        String[] relationCopyTags = { "highway", "name", "ref" };
                        for (String tag : relationCopyTags) {
                            if (relation.hasTag(tag) && !way.hasTag(tag)) {
                                way.addTag(tag, relation.getTag(tag));
                            }
                        }
                        if (relation.isTag("railway", "platform") && !way.hasTag("railway")) {
                            way.addTag("railway", "platform");
                        }
                        if (relation.isTag("public_transport", "platform") && !way.hasTag("public_transport")) {
                            way.addTag("public_transport", "platform");
                        }
                    }
                }
        }
        
        /**
         * Handler for a new Area (single way area or multipolygon relations)
         * 
         * @param area
         */
        private void newArea(Area area) {
            StreetTraversalPermission permissions = getPermissionsForEntity(area.parent,
                    StreetTraversalPermission.PEDESTRIAN_AND_BICYCLE);
            if (isOsmEntityRoutable(area.parent) && (permissions != StreetTraversalPermission.NONE)) {
                this._walkableAreas.add(area);
            }
            if (area.parent.isParkAndRide()) {
                this._parkAndRideAreas.add(area);
            }
        }
        
        /**
         * Copies useful metadata from relations to the relevant ways/nodes.
         */
        private void processRelations() {
            LOG.debug("Processing relations...");
            
            for (OSMRelation relation : this._relations.values()) {
                if (relation.isTag("type", "restriction")) {
                    processRestriction(relation);
                } else if (relation.isTag("type", "level_map")) {
                    processLevelMap(relation);
                } else if (relation.isTag("type", "route")) {
                    processRoad(relation);
                } else if (relation.isTag("type", "public_transport")) {
                    processPublicTransportStopArea(relation);
                }
                
                // multipolygons will be further processed in secondPhase()
            }
        }
        
        /**
         * Store turn restrictions.
         *
         * @param relation
         */
        private void processRestriction(OSMRelation relation) {
            long from = -1, to = -1, via = -1;
            for (OSMRelationMember member : relation.getMembers()) {
                String role = member.getRole();
                if (role.equals("from")) {
                    from = member.getRef();
                } else if (role.equals("to")) {
                    to = member.getRef();
                } else if (role.equals("via")) {
                    via = member.getRef();
                }
            }
            if ((from == -1) || (to == -1) || (via == -1)) {
                LOG.warn(this.graph.addBuilderAnnotation(new TurnRestrictionBad(relation.getId())));
                return;
            }
            
            TraverseModeSet modes = new TraverseModeSet(TraverseMode.BICYCLE, TraverseMode.CAR, TraverseMode.CUSTOM_MOTOR_VEHICLE);
            String exceptModes = relation.getTag("except");
            if (exceptModes != null) {
                for (String m : exceptModes.split(";")) {
                    if (m.equals("motorcar")) {
                        modes.setDriving(false);
                    } else if (m.equals("bicycle")) {
                        modes.setBicycle(false);
                        LOG.debug(this.graph.addBuilderAnnotation(new TurnRestrictionException(via, from)));
                    }
                }
            }
            
            TurnRestrictionTag tag;
            if (relation.isTag("restriction", "no_right_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN, Direction.RIGHT);
            } else if (relation.isTag("restriction", "no_left_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN, Direction.LEFT);
            } else if (relation.isTag("restriction", "no_straight_on")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN, Direction.STRAIGHT);
            } else if (relation.isTag("restriction", "no_u_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.NO_TURN, Direction.U);
            } else if (relation.isTag("restriction", "only_straight_on")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN, Direction.STRAIGHT);
            } else if (relation.isTag("restriction", "only_right_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN, Direction.RIGHT);
            } else if (relation.isTag("restriction", "only_left_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN, Direction.LEFT);
            } else if (relation.isTag("restriction", "only_u_turn")) {
                tag = new TurnRestrictionTag(via, TurnRestrictionType.ONLY_TURN, Direction.U);
            } else {
                LOG.warn(this.graph.addBuilderAnnotation(new TurnRestrictionUnknown(relation.getTag("restriction"))));
                return;
            }
            tag.modes = modes.clone();
            
            // set the time periods for this restriction, if applicable
            if (relation.hasTag("day_on") && relation.hasTag("day_off") && relation.hasTag("hour_on")
                    && relation.hasTag("hour_off")) {
                
                try {
                    tag.time = RepeatingTimePeriod.parseFromOsmTurnRestriction(relation.getTag("day_on"), relation
                            .getTag("day_off"), relation.getTag("hour_on"), relation.getTag("hour_off"));
                } catch (NumberFormatException e) {
                    LOG.info("Unparseable turn restriction: " + relation.getId());
                }
            }
            
            this.turnRestrictionsByFromWay.put(from, tag);
            this.turnRestrictionsByToWay.put(to, tag);
        }
        
        /**
         * Process an OSM level map.
         *
         * @param relation
         */
        private void processLevelMap(OSMRelation relation) {
            Map<String, OSMLevel> levels = OSMLevel.mapFromSpecList(relation.getTag("levels"), Source.LEVEL_MAP, true);
            for (OSMRelationMember member : relation.getMembers()) {
                if ("way".equals(member.getType()) && this._ways.containsKey(member.getRef())) {
                    OSMWay way = this._ways.get(member.getRef());
                    if (way != null) {
                        String role = member.getRole();
                        // if the level map relation has a role:xyz tag, this way is something
                        // more complicated than a single level (e.g. ramp/stairway).
                        if (!relation.hasTag("role:" + role)) {
                            if (levels.containsKey(role)) {
                                this.wayLevels.put(way, levels.get(role));
                            } else {
                                LOG.warn(member.getRef() + " has undefined level " + role);
                            }
                        }
                    }
                }
            }
        }
        
        /*
         * Handle route=road relations.
         * @param relation
         */
        private void processRoad(OSMRelation relation) {
            for (OSMRelationMember member : relation.getMembers()) {
                if (!("way".equals(member.getType()) && this._ways.containsKey(member.getRef()))) {
                    continue;
                }
                
                OSMWithTags way = this._ways.get(member.getRef());
                if (way == null) {
                    continue;
                }
                
                if (relation.hasTag("name")) {
                    if (way.hasTag("otp:route_name")) {
                        way.addTag("otp:route_name", addUniqueName(way.getTag("otp:route_name"), relation.getTag("name")));
                    } else {
                        way.addTag(new OSMTag("otp:route_name", relation.getTag("name")));
                    }
                }
                if (relation.hasTag("ref")) {
                    if (way.hasTag("otp:route_ref")) {
                        way.addTag("otp:route_ref", addUniqueName(way.getTag("otp:route_ref"), relation.getTag("ref")));
                    } else {
                        way.addTag(new OSMTag("otp:route_ref", relation.getTag("ref")));
                    }
                }
            }
        }
        
        /**
         * Process an OSM public transport stop area relation. This goes through all
         * public_transport=stop_area relations and adds the parent (either an area or multipolygon
         * relation) as the key and a Set of transit stop nodes that should be included in the
         * parent area as the value into stopsInAreas. This improves
         * TransitToTaggedStopsGraphBuilder by enabling us to have unconnected stop nodes within the
         * areas by creating relations .
         *
         * @param relation
         * @author hannesj
         * @see "http://wiki.openstreetmap.org/wiki/Tag:public_transport%3Dstop_area"
         */
        private void processPublicTransportStopArea(OSMRelation relation) {
            OSMWithTags platformArea = null;
            Set<OSMNode> platformsNodes = new HashSet<>();
            for (OSMRelationMember member : relation.getMembers()) {
                if ("way".equals(member.getType()) && "platform".equals(member.getRole())
                        && this._areaWayIds.contains(member.getRef())) {
                    if (platformArea == null) {
                        platformArea = this._areaWaysById.get(member.getRef());
                    } else {
                        LOG.warn("Too many areas in relation " + relation.getId());
                    }
                } else if ("relation".equals(member.getType()) && "platform".equals(member.getRole())
                        && this._relations.containsKey(member.getRef())) {
                    if (platformArea == null) {
                        platformArea = this._relations.get(member.getRef());
                    } else {
                        LOG.warn("Too many areas in relation " + relation.getId());
                    }
                } else if ("node".equals(member.getType()) && this._nodes.containsKey(member.getRef())) {
                    platformsNodes.add(this._nodes.get(member.getRef()));
                }
            }
            if ((platformArea != null) && !platformsNodes.isEmpty()) {
                this.stopsInAreas.put(platformArea, platformsNodes);
            } else {
                LOG.warn("Unable to process public transportation relation " + relation.getId());
            }
        }
        
        private String addUniqueName(String routes, String name) {
            String[] names = routes.split(", ");
            for (String existing : names) {
                if (existing.equals(name)) { return routes; }
            }
            return routes + ", " + name;
        }
        
        /**
         * Returns the length of the geometry in meters.
         *
         * @param geometry
         * @return
         */
        private double getGeometryLengthMeters(Geometry geometry) {
            Coordinate[] coordinates = geometry.getCoordinates();
            double d = 0;
            for (int i = 1; i < coordinates.length; ++i) {
                d += this.distanceLibrary.distance(coordinates[i - 1], coordinates[i]);
            }
            return d;
        }
        
        /**
         * Handle oneway streets, cycleways, and other per-mode and universal access controls. See
         * http://wiki.openstreetmap.org/wiki/Bicycle for various scenarios, along with
         * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing#Oneway.
         *
         * @param end
         * @param start
         */
        private P2<PlainStreetEdge> getEdgesForStreet(IntersectionVertex start, IntersectionVertex end, OSMWay way, int index,
                long startNode, long endNode, StreetTraversalPermission permissions, LineString geometry) {
            // No point in returning edges that can't be traversed by anyone.
            if (permissions.allowsNothing()) { return new P2<PlainStreetEdge>(null, null); }
            
            LineString backGeometry = (LineString) geometry.reverse();
            PlainStreetEdge street = null, backStreet = null;
            double length = this.getGeometryLengthMeters(geometry);
            
            P2<StreetTraversalPermission> permissionPair = getPermissions(permissions, way);
            StreetTraversalPermission permissionsFront = permissionPair.getFirst();
            StreetTraversalPermission permissionsBack = permissionPair.getSecond();
            
            if (permissionsFront.allowsAnything()) {
                street = getEdgeForStreet(start, end, way, index, startNode, endNode, length, permissionsFront, geometry, false);
            }
            if (permissionsBack.allowsAnything()) {
                backStreet = getEdgeForStreet(end, start, way, index, endNode, startNode, length, permissionsBack, backGeometry,
                        true);
            }
            
            /* mark edges that are on roundabouts */
            if (way.isRoundabout()) {
                if (street != null) {
                    street.setRoundabout(true);
                }
                if (backStreet != null) {
                    backStreet.setRoundabout(true);
                }
            }
            
            return new P2<PlainStreetEdge>(street, backStreet);
        }
        
        /**
         * Check OSM tags for various one-way and one-way-by-mode tags and return a pair of
         * permissions for travel along and against the way.
         */
        private P2<StreetTraversalPermission> getPermissions(StreetTraversalPermission permissions, OSMWay way) {
            
            StreetTraversalPermission permissionsFront = permissions;
            StreetTraversalPermission permissionsBack = permissions;
            
            // Check driving direction restrictions.
            if (way.isOneWayForwardDriving() || way.isRoundabout()) {
                permissionsBack = permissionsBack.remove(StreetTraversalPermission.BICYCLE_AND_DRIVING);
            }
            if (way.isOneWayReverseDriving()) {
                permissionsFront = permissionsFront.remove(StreetTraversalPermission.BICYCLE_AND_DRIVING);
            }
            
            // Check bike direction restrictions.
            if (way.isOneWayForwardBicycle()) {
                permissionsBack = permissionsBack.remove(StreetTraversalPermission.BICYCLE);
            }
            if (way.isOneWayReverseBicycle()) {
                permissionsFront = permissionsFront.remove(StreetTraversalPermission.BICYCLE);
            }
            
            // TODO(flamholz): figure out what this is for.
            String oneWayBicycle = way.getTag("oneway:bicycle");
            if (OSMWithTags.isFalse(oneWayBicycle) || way.isTagTrue("bicycle:backwards")) {
                if (permissions.allows(StreetTraversalPermission.BICYCLE)) {
                    permissionsFront = permissionsFront.add(StreetTraversalPermission.BICYCLE);
                    permissionsBack = permissionsBack.add(StreetTraversalPermission.BICYCLE);
                }
            }
            
            if (way.isOpposableCycleway()) {
                permissionsBack = permissionsBack.add(StreetTraversalPermission.BICYCLE);
            }
            return new P2<StreetTraversalPermission>(permissionsFront, permissionsBack);
        }
        
        private PlainStreetEdge getEdgeForStreet(IntersectionVertex start, IntersectionVertex end, OSMWay way, int index,
                long startNode, long endNode, double length, StreetTraversalPermission permissions, LineString geometry,
                boolean back) {
            
            String label = "way " + way.getId() + " from " + index;
            label = unique(label);
            String name = getNameForWay(way, label);
            
            // consider the elevation gain of stairs, roughly
            boolean steps = way.isSteps();
            if (steps) {
                length *= 2;
            }
            
            float carSpeed = OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getCarSpeedForWay(way, back);
            
            PlainStreetEdge street = OpenStreetMapGraphBuilderImpl.this.edgeFactory.createEdge(this._nodes.get(startNode),
                    this._nodes.get(endNode), way, start, end, geometry, name, length, permissions, back, carSpeed);
            
            String highway = way.getTag("highway");
            int cls;
            if ("crossing".equals(highway) && !way.isTag("bicycle", "designated")) {
                cls = StreetEdge.CLASS_CROSSING;
            } else if ("footway".equals(highway) && way.isTag("footway", "crossing") && !way.isTag("bicycle", "designated")) {
                cls = StreetEdge.CLASS_CROSSING;
            } else if ("residential".equals(highway) || "tertiary".equals(highway) || "secondary".equals(highway)
                    || "secondary_link".equals(highway) || "primary".equals(highway) || "primary_link".equals(highway)
                    || "trunk".equals(highway) || "trunk_link".equals(highway)) {
                cls = StreetEdge.CLASS_STREET;
            } else {
                cls = StreetEdge.CLASS_OTHERPATH;
            }
            
            cls |= getStreetClasses(way);
            street.setStreetClass(cls);
            
            if (!way.hasTag("name") && !way.hasTag("ref")) {
                street.setHasBogusName(true);
            }
            street.setStairs(steps);
            
            if (way.isTagTrue("toll") || way.isTagTrue("toll:motorcar")) {
                street.setToll(true);
            } else {
                street.setToll(false);
            }
            
            /* TODO: This should probably generalized somehow? */
            if (!OpenStreetMapGraphBuilderImpl.this.ignoreWheelchairAccessibility
                    && (way.isTagFalse("wheelchair") || (steps && !way.isTagTrue("wheelchair")))) {
                street.setWheelchairAccessible(false);
            }
            
            street.setSlopeOverride(OpenStreetMapGraphBuilderImpl.this.wayPropertySet.getSlopeOverride(way));
            
            // < 0.04: account for
            if (carSpeed < 0.04) {
                LOG.warn(this.graph.addBuilderAnnotation(new StreetCarSpeedZero(way.getId())));
            }
            
            if (OpenStreetMapGraphBuilderImpl.this.customNamer != null) {
                OpenStreetMapGraphBuilderImpl.this.customNamer.nameWithEdge(way, street);
            }
            
            return street;
        }
        
        private int getStreetClasses(OSMWithTags way) {
            int link = 0;
            String highway = way.getTag("highway");
            if ((highway != null) && highway.endsWith(("_link"))) {
                link = StreetEdge.CLASS_LINK;
            }
            return getPlatformClass(way) | link;
        }
        
        private int getPlatformClass(OSMWithTags way) {
            String highway = way.getTag("highway");
            if ("platform".equals(way.getTag("railway"))) { return StreetEdge.CLASS_TRAIN_PLATFORM; }
            if ("platform".equals(highway) || "platform".equals(way.getTag("public_transport"))) {
                if (way.isTagTrue("train") || way.isTagTrue("subway") || way.isTagTrue("tram") || way.isTagTrue("monorail")) { return StreetEdge.CLASS_TRAIN_PLATFORM; }
                return StreetEdge.CLASS_OTHER_PLATFORM;
            }
            return 0;
        }
        
        private String getNameForWay(OSMWithTags way, String id) {
            String name = way.getAssumedName();
            
            if (OpenStreetMapGraphBuilderImpl.this.customNamer != null) {
                name = OpenStreetMapGraphBuilderImpl.this.customNamer.name(way, name);
            }
            
            if (name == null) {
                name = id;
            }
            return name;
        }
        
        private StreetTraversalPermission getPermissionsForEntity(OSMWithTags entity, StreetTraversalPermission def) {
            StreetTraversalPermission permission = null;
            
            /*
             * Only a few tags are examined here, because we only care about modes supported by OTP
             * (wheelchairs are not of concern here) Only a few values are checked for, all other
             * values are presumed to be permissive (=> This may not be perfect, but is closer to
             * reality, since most people don't follow the rules perfectly ;-)
             */
            if (entity.isGeneralAccessDenied()) {
                // this can actually be overridden
                permission = StreetTraversalPermission.NONE;
                if (entity.isMotorcarExplicitlyAllowed()) {
                    permission = permission.add(StreetTraversalPermission.ALL_DRIVING);
                }
                if (entity.isBicycleExplicitlyAllowed()) {
                    permission = permission.add(StreetTraversalPermission.BICYCLE);
                }
                if (entity.isPedestrianExplicitlyAllowed()) {
                    permission = permission.add(StreetTraversalPermission.PEDESTRIAN);
                }
            } else {
                permission = def;
            }
            
            if (entity.isMotorcarExplicitlyDenied()) {
                permission = permission.remove(StreetTraversalPermission.ALL_DRIVING);
            } else if (entity.hasTag("motorcar")) {
                permission = permission.add(StreetTraversalPermission.ALL_DRIVING);
            }
            
            if (entity.isBicycleExplicitlyDenied()) {
                permission = permission.remove(StreetTraversalPermission.BICYCLE);
            } else if (entity.hasTag("bicycle")) {
                permission = permission.add(StreetTraversalPermission.BICYCLE);
            }
            
            if (entity.isPedestrianExplicitlyDenied()) {
                permission = permission.remove(StreetTraversalPermission.PEDESTRIAN);
            } else if (entity.hasTag("foot")) {
                permission = permission.add(StreetTraversalPermission.PEDESTRIAN);
            }
            
            if (entity.isUnderConstruction()) {
                permission = StreetTraversalPermission.NONE;
            }
            
            if (permission == null) { return def; }
            
            return permission;
        }
        
        /**
         * Computes permissions for an OSMWay.
         *
         * @param way
         * @param def
         * @return
         */
        private StreetTraversalPermission getPermissionsForWay(OSMWay way, StreetTraversalPermission def) {
            StreetTraversalPermission permissions = getPermissionsForEntity(way, def);
            
            /*
             * pedestrian rules: everything is two-way (assuming pedestrians are allowed at all)
             * bicycle rules: default: permissions; cycleway=dismount means walk your bike -- the
             * engine will automatically try walking bikes any time it is forbidden to ride them, so
             * the only thing to do here is to remove bike permissions oneway=... sets permissions
             * for cars and bikes oneway:bicycle overwrites these permissions for bikes only now,
             * cycleway=opposite_lane, opposite, opposite_track can allow once oneway has been set
             * by oneway:bicycle, but should give a warning if it conflicts with oneway:bicycle
             * bicycle:backward=yes works like oneway:bicycle=no bicycle:backwards=no works like
             * oneway:bicycle=yes
             */
            
            // Compute pedestrian permissions.
            if (way.isPedestrianExplicitlyAllowed()) {
                permissions = permissions.add(StreetTraversalPermission.PEDESTRIAN);
            } else if (way.isPedestrianExplicitlyDenied()) {
                permissions = permissions.remove(StreetTraversalPermission.PEDESTRIAN);
            }
            
            // Compute bike permissions, check consistency.
            boolean forceBikes = false;
            if (way.isBicycleExplicitlyAllowed()) {
                permissions = permissions.add(StreetTraversalPermission.BICYCLE);
                forceBikes = true;
            }
            
            if (way.isBicycleDismountForced()) {
                permissions = permissions.remove(StreetTraversalPermission.BICYCLE);
                if (forceBikes) {
                    LOG.warn(this.graph.addBuilderAnnotation(new ConflictingBikeTags(way.getId())));
                }
            }
            
            return permissions;
        }
        
        /**
         * Record the level of the way for this node, e.g. if the way is at level 5, mark that this
         * node is active at level 5.
         *
         * @param way the way that has the level
         * @param node the node to record for
         * @author mattwigway
         */
        private IntersectionVertex recordLevel(OSMNode node, OSMWithTags way) {
            OSMLevel level = this.wayLevels.get(way);
            HashMap<OSMLevel, IntersectionVertex> vertices;
            long nodeId = node.getId();
            if (this.multiLevelNodes.containsKey(nodeId)) {
                vertices = this.multiLevelNodes.get(nodeId);
            } else {
                vertices = new HashMap<OSMLevel, IntersectionVertex>();
                this.multiLevelNodes.put(nodeId, vertices);
            }
            if (!vertices.containsKey(level)) {
                Coordinate coordinate = getCoordinate(node);
                String label = this.getLevelNodeLabel(node, level);
                IntersectionVertex vertex = new IntersectionVertex(this.graph, label, coordinate.x, coordinate.y, label);
                vertices.put(level, vertex);
                // multilevel nodes should also undergo turn-conversion
                this.endpoints.add(vertex);
                return vertex;
            }
            return vertices.get(level);
        }
        
        /**
         * Make or get a shared vertex for flat intersections, or one vertex per level for
         * multilevel nodes like elevators. When there is an elevator or other Z-dimension
         * discontinuity, a single node can appear in several ways at different levels.
         *
         * @param node The node to fetch a label for.
         * @param way The way it is connected to (for fetching level information).
         * @return vertex The graph vertex.
         */
        private IntersectionVertex getVertexForOsmNode(OSMNode node, OSMWithTags way) {
            // If the node should be decomposed to multiple levels,
            // use the numeric level because it is unique, the human level may not be (although
            // it will likely lead to some head-scratching if it is not).
            IntersectionVertex iv = null;
            if (node.isMultiLevel()) {
                // make a separate node for every level
                return recordLevel(node, way);
            }
            // single-level case
            long nid = node.getId();
            iv = this.intersectionNodes.get(nid);
            if (iv == null) {
                Coordinate coordinate = getCoordinate(node);
                String label = getNodeLabel(node);
                String highway = node.getTag("highway");
                if ("motorway_junction".equals(highway)) {
                    String ref = node.getTag("ref");
                    if (ref != null) {
                        ExitVertex ev = new ExitVertex(this.graph, label, coordinate.x, coordinate.y);
                        ev.setExitName(ref);
                        iv = ev;
                    }
                }
                
                if (node.isStop()) {
                    String ref = node.getTag("ref");
                    String name = node.getTag("name");
                    if (ref != null) {
                        TransitStopStreetVertex tsv = new TransitStopStreetVertex(this.graph, label, coordinate.x, coordinate.y,
                                name, ref);
                        iv = tsv;
                    }
                }
                
                if (iv == null) {
                    iv = new IntersectionVertex(this.graph, label, coordinate.x, coordinate.y, label);
                    if (node.hasTrafficLight()) {
                        iv.trafficLight = (true);
                    }
                }
                
                this.intersectionNodes.put(nid, iv);
                this.endpoints.add(iv);
            }
            return iv;
        }
        
        @Override
        public void doneRelations() {
            // nothing to do here
        }
    }
    
    @Override
    public void checkInputs() {
        for (OpenStreetMapProvider provider : this._providers) {
            provider.checkInputs();
        }
    }
}
