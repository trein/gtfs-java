package com.trein.gtfs.otp.building.graph.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trein.gtfs.otp.building.graph.api.TraverseMode;
import com.trein.gtfs.otp.building.graph.osm.model.P2;

public class NetworkLinker {
    
    private static Logger LOG = LoggerFactory.getLogger(NetworkLinker.class);
    
    private final Graph graph;
    
    private final NetworkLinkerLibrary networkLinkerLibrary;
    
    public NetworkLinker(Graph graph, HashMap<Class<?>, Object> extra) {
        this.graph = graph;
        this.networkLinkerLibrary = new NetworkLinkerLibrary(graph, extra);
        this.networkLinkerLibrary.options = new RoutingRequest(TraverseMode.BICYCLE);
    }
    
    public NetworkLinker(Graph graph) {
        // we should be using Collections.emptyMap(), but it breaks Java's broken-ass type checker
        this(graph, new HashMap<Class<?>, Object>());
    }
    
    /**
     * Link the transit network to the street network. Connect each transit vertex to the nearest
     * Street edge with a StreetTransitLink.
     */
    public void createLinkage() {
        
        LOG.debug("creating linkages...");
        // iterate over a copy of vertex list because it will be modified
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        vertices.addAll(this.graph.getVertices());
        
        for (TransitStop ts : IterableLibrary.filter(vertices, TransitStop.class)) {
            // if the street is already linked there is no need to linked it again,
            // could happened if using the prune isolated island
            boolean alreadyLinked = false;
            for (Edge e : ts.getOutgoing()) {
                if (e instanceof StreetTransitLink) {
                    alreadyLinked = true;
                    break;
                }
            }
            if (alreadyLinked) {
                continue;
            }
            // only connect transit stops that (a) are entrances, or (b) have no associated
            // entrances
            if (ts.isEntrance() || !ts.hasEntrances()) {
                boolean wheelchairAccessible = ts.hasWheelchairEntrance();
                if (!this.networkLinkerLibrary.connectVertexToStreets(ts, wheelchairAccessible).getResult()) {
                    LOG.warn(this.graph.addBuilderAnnotation(new StopUnlinked(ts)));
                }
            }
        }
        // remove replaced edges
        for (HashSet<StreetEdge> toRemove : this.networkLinkerLibrary.replacements.keySet()) {
            for (StreetEdge edge : toRemove) {
                edge.getFromVertex().removeOutgoing(edge);
                edge.getToVertex().removeIncoming(edge);
            }
        }
        // and add back in replacements
        for (LinkedList<P2<PlainStreetEdge>> toAdd : this.networkLinkerLibrary.replacements.values()) {
            for (P2<PlainStreetEdge> edges : toAdd) {
                PlainStreetEdge edge1 = edges.getFirst();
                if (edge1.getToVertex().getLabel().startsWith("split ") || edge1.getFromVertex().getLabel().startsWith("split ")) {
                    continue;
                }
                edge1.getFromVertex().addOutgoing(edge1);
                edge1.getToVertex().addIncoming(edge1);
                PlainStreetEdge edge2 = edges.getSecond();
                if (edge2 != null) {
                    edge2.getFromVertex().addOutgoing(edge2);
                    edge2.getToVertex().addIncoming(edge2);
                }
            }
        }
        
        FindMaxWalkDistances.find(this.graph);
        
        LOG.debug("Linking bike rental stations...");
        for (BikeRentalStationVertex brsv : IterableLibrary.filter(vertices, BikeRentalStationVertex.class)) {
            if (!this.networkLinkerLibrary.connectVertexToStreets(brsv).getResult()) {
                LOG.warn(this.graph.addBuilderAnnotation(new BikeRentalStationUnlinked(brsv)));
            }
        }
    }
}
