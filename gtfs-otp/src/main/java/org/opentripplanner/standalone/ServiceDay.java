package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;

/**
 * Represents a day of transit services. Intended for quickly checking whether a service is running
 * during path searches.
 *
 * @author andrewbyrd
 */
public class ServiceDay implements Serializable {
    private static final long serialVersionUID = -1206371243806996680L;
    
    protected long midnight;
    protected ServiceDate serviceDate;
    protected BitSet serviceIdsRunning;
    
    /*
     * make a ServiceDay including the given time's day's starting second and a set of serviceIds
     * running on that day.
     */
    public ServiceDay(Graph graph, long time, CalendarService cs, String agencyId) {
        TimeZone timeZone = cs.getTimeZoneForAgencyId(agencyId);
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.setTime(new Date(time * 1000));
        this.serviceDate = new ServiceDate(calendar);
        
        init(graph, cs, timeZone);
    }
    
    public ServiceDay(Graph graph, ServiceDate serviceDate, CalendarService cs, String agencyId) {
        TimeZone timeZone = cs.getTimeZoneForAgencyId(agencyId);
        this.serviceDate = new ServiceDate(serviceDate);
        
        init(graph, cs, timeZone);
    }
    
    private void init(Graph graph, CalendarService cs, TimeZone timeZone) {
        Date d = this.serviceDate.getAsDate(timeZone);
        this.midnight = d.getTime() / 1000;
        this.serviceIdsRunning = new BitSet(cs.getServiceIds().size());

        for (AgencyAndId serviceId : cs.getServiceIdsOnDate(this.serviceDate)) {
            int n = graph.serviceCodes.get(serviceId);
            if (n < 0) {
                continue;
            }
            this.serviceIdsRunning.set(n);
        }
    }
    
    /** Does the given serviceId run on this ServiceDay? */
    public boolean serviceRunning(int serviceCode) {
        return this.serviceIdsRunning.get(serviceCode);
    }
    
    /** Do any of the services for this set of service codes run on this ServiceDay? */
    public boolean anyServiceRunning(BitSet serviceCodes) {
        return this.serviceIdsRunning.intersects(serviceCodes);
    }
    
    /**
     * Return the ServiceDate for this ServiceDay.
     */
    public ServiceDate getServiceDate() {
        return this.serviceDate;
    }

    /*
     * Return number of seconds after midnight on this ServiceDay for the given time. Note that the
     * parameter and the return value are in seconds since the epoch Return value may be negative,
     * indicating that the time is before this ServiceDay.
     */
    public int secondsSinceMidnight(long time) {
        return (int) (time - this.midnight);
    }

    /*
     * Return number of seconds since the epoch based on the given number of seconds after midnight
     * on this ServiceDay Input value may be negative, indicating that the time is before this
     * ServiceDay.
     */
    public long time(int secondsSinceMidnight) {
        return this.midnight + secondsSinceMidnight;
    }

    @Override
    public String toString() {
        return Long.toString(this.midnight) + Arrays.asList(this.serviceIdsRunning);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServiceDay)) { return false; }
        ServiceDay other = (ServiceDay) o;
        return other.midnight == this.midnight;
    }

    @Override
    public int hashCode() {
        return (int) this.midnight;
    }

}
