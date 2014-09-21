package org.opentripplanner.standalone;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;

/**
 * This adds a note to all boardings of a given route or stop (optionally, in a given direction)
 *
 * @author novalis
 */
@XmlRootElement(name = "AlertPatch")
public class AlertPatch implements Serializable {
    private static final long serialVersionUID = 20140319L;

    private String id;

    private Alert alert;

    private List<TimePeriod> timePeriods = new ArrayList<TimePeriod>();

    private String agency;

    private AgencyAndId route;

    private AgencyAndId trip;

    private AgencyAndId stop;

    private String direction;

    @XmlElement
    public Alert getAlert() {
        return this.alert;
    }

    public boolean displayDuring(State state) {
        for (TimePeriod timePeriod : this.timePeriods) {
            if (state.getTimeSeconds() >= timePeriod.startTime) {
                if (state.getStartTimeSeconds() < timePeriod.endTime) { return true; }
            }
        }
        return false;
    }

    @XmlElement
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void apply(Graph graph) {
        Agency agency = this.agency != null ? graph.index.agencyForId.get(this.agency) : null;
        Route route = this.route != null ? graph.index.routeForId.get(this.route) : null;
        Stop stop = this.stop != null ? graph.index.stopForId.get(this.stop) : null;
        Trip trip = this.trip != null ? graph.index.tripForId.get(this.trip) : null;

        if ((route != null) || (trip != null) || (agency != null)) {
            Collection<TripPattern> tripPatterns;

            if (trip != null) {
                tripPatterns = new LinkedList<TripPattern>();
                TripPattern tripPattern = graph.index.patternForTrip.get(trip);
                if (tripPattern != null) {
                    tripPatterns.add(tripPattern);
                }
            } else if (route != null) {
                tripPatterns = graph.index.patternsForRoute.get(route);
            } else {
                tripPatterns = graph.index.patternsForAgency.get(agency);
            }

            for (TripPattern tripPattern : tripPatterns) {
                if ((this.direction != null) && !this.direction.equals(tripPattern.getDirection())) {
                    continue;
                }
                for (int i = 0; i < tripPattern.stopPattern.stops.length; i++) {
                    if ((stop == null) || stop.equals(tripPattern.stopPattern.stops[i])) {
                        graph.addAlertPatch(tripPattern.boardEdges[i], this);
                        graph.addAlertPatch(tripPattern.alightEdges[i], this);
                    }
                }
            }
        } else if (stop != null) {
            TransitStop transitStop = graph.index.stopVertexForStop.get(stop);

            for (Edge edge : transitStop.getOutgoing()) {
                if (edge instanceof PreBoardEdge) {
                    graph.addAlertPatch(edge, this);
                    break;
                }
            }

            for (Edge edge : transitStop.getIncoming()) {
                if (edge instanceof PreAlightEdge) {
                    graph.addAlertPatch(edge, this);
                    break;
                }
            }
        }
    }

    public void remove(Graph graph) {
        Agency agency = this.agency != null ? graph.index.agencyForId.get(this.agency) : null;
        Route route = this.route != null ? graph.index.routeForId.get(this.route) : null;
        Stop stop = this.stop != null ? graph.index.stopForId.get(this.stop) : null;
        Trip trip = this.trip != null ? graph.index.tripForId.get(this.trip) : null;

        if ((route != null) || (trip != null) || (agency != null)) {
            Collection<TripPattern> tripPatterns;

            if (trip != null) {
                tripPatterns = new LinkedList<TripPattern>();
                TripPattern tripPattern = graph.index.patternForTrip.get(trip);
                if (tripPattern != null) {
                    tripPatterns.add(tripPattern);
                }
            } else if (route != null) {
                tripPatterns = graph.index.patternsForRoute.get(route);
            } else {
                tripPatterns = graph.index.patternsForAgency.get(agency);
            }

            for (TripPattern tripPattern : tripPatterns) {
                if ((this.direction != null) && !this.direction.equals(tripPattern.getDirection())) {
                    continue;
                }
                for (int i = 0; i < tripPattern.stopPattern.stops.length; i++) {
                    if ((stop == null) || stop.equals(tripPattern.stopPattern.stops[i])) {
                        graph.removeAlertPatch(tripPattern.boardEdges[i], this);
                        graph.removeAlertPatch(tripPattern.alightEdges[i], this);
                    }
                }
            }
        } else if (stop != null) {
            TransitStop transitStop = graph.index.stopVertexForStop.get(stop);

            for (Edge edge : transitStop.getOutgoing()) {
                if (edge instanceof PreBoardEdge) {
                    graph.removeAlertPatch(edge, this);
                    break;
                }
            }

            for (Edge edge : transitStop.getIncoming()) {
                if (edge instanceof PreAlightEdge) {
                    graph.removeAlertPatch(edge, this);
                    break;
                }
            }
        }
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        if (this.timePeriods instanceof ArrayList<?>) {
            ((ArrayList<TimePeriod>) this.timePeriods).trimToSize();
        }
        os.defaultWriteObject();
    }

    public void setTimePeriods(List<TimePeriod> periods) {
        this.timePeriods = periods;
    }

    public String getAgency() {
        return this.agency;
    }

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    public AgencyAndId getRoute() {
        return this.route;
    }

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    public AgencyAndId getTrip() {
        return this.trip;
    }

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    public AgencyAndId getStop() {
        return this.stop;
    }

    public void setAgencyId(String agency) {
        this.agency = agency;
    }

    public void setRoute(AgencyAndId route) {
        this.route = route;
    }

    public void setTrip(AgencyAndId trip) {
        this.trip = trip;
    }

    public void setDirection(String direction) {
        if ((direction != null) && direction.equals("")) {
            direction = null;
        }
        this.direction = direction;
    }

    @XmlElement
    public String getDirection() {
        return this.direction;
    }

    public void setStop(AgencyAndId stop) {
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AlertPatch)) { return false; }
        AlertPatch other = (AlertPatch) o;
        if (this.direction == null) {
            if (other.direction != null) { return false; }
        } else {
            if (!this.direction.equals(other.direction)) { return false; }
        }
        if (this.agency == null) {
            if (other.agency != null) { return false; }
        } else {
            if (!this.agency.equals(other.agency)) { return false; }
        }
        if (this.trip == null) {
            if (other.trip != null) { return false; }
        } else {
            if (!this.trip.equals(other.trip)) { return false; }
        }
        if (this.stop == null) {
            if (other.stop != null) { return false; }
        } else {
            if (!this.stop.equals(other.stop)) { return false; }
        }
        if (this.route == null) {
            if (other.route != null) { return false; }
        } else {
            if (!this.route.equals(other.route)) { return false; }
        }
        if (this.alert == null) {
            if (other.alert != null) { return false; }
        } else {
            if (!this.alert.equals(other.alert)) { return false; }
        }
        if (this.id == null) {
            if (other.id != null) { return false; }
        } else {
            if (!this.id.equals(other.id)) { return false; }
        }
        if (this.timePeriods == null) {
            if (other.timePeriods != null) { return false; }
        } else {
            if (!this.timePeriods.equals(other.timePeriods)) { return false; }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return ((this.direction == null ? 0 : this.direction.hashCode()) + (this.agency == null ? 0 : this.agency.hashCode())
                + (this.trip == null ? 0 : this.trip.hashCode()) + (this.stop == null ? 0 : this.stop.hashCode())
                + (this.route == null ? 0 : this.route.hashCode()) + (this.alert == null ? 0 : this.alert.hashCode()));
    }
}
