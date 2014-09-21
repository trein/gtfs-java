package org.opentripplanner.standalone;

import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This index is used in Analyst and does not need to be instantiated if you are not performing
 * Analyst requests.
 */
public class GeometryIndex implements GeometryIndexService {

    private static final Logger LOG = LoggerFactory.getLogger(GeometryIndex.class);
    private static final double SEARCH_RADIUS_M = 100; // meters
    private static final double SEARCH_RADIUS_DEG = SphericalDistanceLibrary.metersToDegrees(SEARCH_RADIUS_M);
    
    GraphService graphService;

    private final STRtree pedestrianIndex;
    
    public GeometryIndex(Graph graph) {
        if (graph == null) {
            String message = "Could not retrieve default Graph from GraphService. Check its configuration.";
            LOG.error(message);
            throw new IllegalStateException(message);
        }
        Map<ReversibleLineStringWrapper, StreetEdge> edges = Maps.newHashMap();
        for (StreetVertex vertex : IterableLibrary.filter(graph.getVertices(), StreetVertex.class)) {
            for (StreetEdge e : IterableLibrary.filter(vertex.getOutgoing(), StreetEdge.class)) {
                LineString geom = e.getGeometry();
                if (e.getPermission().allows(StreetTraversalPermission.PEDESTRIAN)) {
                    edges.put(new ReversibleLineStringWrapper(geom), e);
                }
            }
        }
        // insert unique edges
        this.pedestrianIndex = new STRtree();
        for (StreetEdge e : edges.values()) {
            LineString geom = e.getGeometry();
            this.pedestrianIndex.insert(geom.getEnvelopeInternal(), e);
        }
        this.pedestrianIndex.build();
        LOG.debug("spatial index size: {}", this.pedestrianIndex.size());
    }

    @SuppressWarnings("rawtypes")
    public List queryPedestrian(Envelope env) {
        return this.pedestrianIndex.query(env);
    }

    @Override
    public BoundingBox getBoundingBox(CoordinateReferenceSystem crs) {
        try {
            Envelope bounds = (Envelope) this.pedestrianIndex.getRoot().getBounds();
            ReferencedEnvelope refEnv = new ReferencedEnvelope(bounds, CRS.decode("EPSG:4326", true));
            return refEnv.toBounds(crs);
        } catch (Exception e) {
            LOG.error("error transforming graph bounding box to request CRS : {}", crs);
            return null;
        }
    }

}
