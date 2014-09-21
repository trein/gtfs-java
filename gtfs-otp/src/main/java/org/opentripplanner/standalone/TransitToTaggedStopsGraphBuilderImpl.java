package org.opentripplanner.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class TransitToTaggedStopsGraphBuilderImpl implements GraphBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(TransitToTaggedStopsGraphBuilderImpl.class);

    StreetVertexIndexServiceImpl index;
    private double searchRadiusM = 250;
    private double searchRadiusLat = SphericalDistanceLibrary.metersToDegrees(searchRadiusM);

    public List<String> provides() {
        return Arrays.asList("street to transit", "linking");
    }

    public List<String> getPrerequisites() {
        return Arrays.asList("streets"); // why not "transit" ?
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        LOG.info("Linking transit stops to tagged bus stops...");

        index = new StreetVertexIndexServiceImpl(graph);
        index.setup();

        // iterate over a copy of vertex list because it will be modified
        ArrayList<Vertex> vertices = new ArrayList<>();
        vertices.addAll(graph.getVertices());

        for (TransitStop ts : IterableLibrary.filter(vertices, TransitStop.class)) {
            // if the street is already linked there is no need to linked it again,
            // could happened if using the prune isolated island
            boolean alreadyLinked = false;
            for(Edge e:ts.getOutgoing()){
                if(e instanceof StreetTransitLink) {
                    alreadyLinked = true;
                    break;
                }
            }
            if(alreadyLinked) continue;
            // only connect transit stops that (a) are entrances, or (b) have no associated
            // entrances
            if (ts.isEntrance() || !ts.hasEntrances()) {
                boolean wheelchairAccessible = ts.hasWheelchairEntrance();
                if (!connectVertexToStop(ts, wheelchairAccessible)) {
                    LOG.debug("Could not connect " + ts.getStopCode() + " at " + ts.getCoordinate().toString());
                    //LOG.warn(graph.addBuilderAnnotation(new StopUnlinked(ts)));
                }
            }
        }
    }

    private boolean connectVertexToStop(TransitStop ts, boolean wheelchairAccessible) {
        String stopCode = ts.getStopCode();
        if (stopCode == null){
            return false;
        }
        Envelope envelope = new Envelope(ts.getCoordinate());
        double xscale = Math.cos(ts.getCoordinate().y * Math.PI / 180);
        envelope.expandBy(searchRadiusLat / xscale, searchRadiusLat);
        Collection<Vertex> vertices = index.getVerticesForEnvelope(envelope);
        for (Vertex v : vertices){
            if (!(v instanceof TransitStopStreetVertex)){
                continue;
            }
            TransitStopStreetVertex tsv = (TransitStopStreetVertex) v;

            // Only use stop codes for linking TODO: find better method to connect stops without stop code
            if (tsv.stopCode != null && tsv.stopCode.equals(stopCode)) {
                new StreetTransitLink(ts, tsv, wheelchairAccessible);
                new StreetTransitLink(tsv, ts, wheelchairAccessible);
                LOG.debug("Connected " + ts.toString() + " to " + tsv.getLabel());
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkInputs() {
        //no inputs
    }
}