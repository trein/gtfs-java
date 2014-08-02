package com.trein.gtfs.orm.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Dates for service IDs using a weekly schedule. Specify when service starts and ends, as well as
 * days of the week where service is available.
 *
 * @author trein
 */
@Entity(name = "calendars")
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Column(name = "monday")
    private boolean monday;

    @Column(name = "tuesday")
    private boolean tuesday;

    @Column(name = "wednesday")
    private boolean wednesday;

    @Column(name = "thursday")
    private boolean thursday;

    @Column(name = "friday")
    private boolean friday;

    @Column(name = "saturday")
    private boolean saturday;

    @Column(name = "sunday")
    private boolean sunday;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    Calendar() {
        
    }

    public Calendar(String serviceId, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday,
            boolean saturday, boolean sunday, Date startDate, Date endDate) {
        this.serviceId = serviceId;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getId() {
        return this.id;
    }

    /**
     * service_id Required The service_id contains an ID that uniquely identifies a set of dates
     * when service is available for one or more routes. Each service_id value can appear at most
     * once in a calendar.txt file. This value is dataset unique. It is referenced by the trips.txt
     * file.
     */
    public String getServiceId() {
        return this.serviceId;
    }

    /**
     * monday Required The monday field contains a binary value that indicates whether the service
     * is valid for all Mondays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Mondays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Mondays in the date range.
     * </pre>
     *
     * Note: You may list exceptions for particular dates, such as holidays, in the
     * calendar_dates.txt file.
     */
    public boolean isAvailableOnMonday() {
        return this.monday;
    }

    /**
     * tuesday Required The tuesday field contains a binary value that indicates whether the service
     * is valid for all Tuesdays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Tuesdays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Tuesdays in the date range.
     * </pre>
     *
     * Note: You may list exceptions for particular dates, such as holidays, in the
     * calendar_dates.txt file.
     */
    public boolean isAvailableOnTuesday() {
        return this.tuesday;
    }

    /**
     * wednesday Required The wednesday field contains a binary value that indicates whether the
     * service is valid for all Wednesdays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Wednesdays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Wednesdays in the date range.
     * Note: You may list exceptions for particular dates, such as holidays, in the calendar_dates.txt file.
     */
    public boolean isAvailableOnWednesday() {
        return this.wednesday;
    }

    /**
     * thursday Required The thursday field contains a binary value that indicates whether the
     * service is valid for all Thursdays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Thursdays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Thursdays in the date range.
     * Note: You may list exceptions for particular dates, such as holidays, in the calendar_dates.txt file.
     */
    public boolean isAvailableOnThursday() {
        return this.thursday;
    }

    /**
     * friday Required The friday field contains a binary value that indicates whether the service
     * is valid for all Fridays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Fridays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Fridays in the date range.
     * </pre>
     *
     * Note: You may list exceptions for particular dates, such as holidays, in the
     * calendar_dates.txt file
     */
    public boolean isAvailableOnFriday() {
        return this.friday;
    }

    /**
     * saturday Required The saturday field contains a binary value that indicates whether the
     * service is valid for all Saturdays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Saturdays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Saturdays in the date range.
     * </pre>
     *
     * Note: You may list exceptions for particular dates, such as holidays, in the
     * calendar_dates.txt file.
     */
    public boolean isAvailableOnSaturday() {
        return this.saturday;
    }

    /**
     * sunday Required The sunday field contains a binary value that indicates whether the service
     * is valid for all Sundays.
     *
     * <pre>
     *     A value of 1 indicates that service is available for all Sundays in the date range. (The date range is specified using the start_date and end_date fields.)
     *     A value of 0 indicates that service is not available on Sundays in the date range.
     * </pre>
     *
     * Note: You may list exceptions for particular dates, such as holidays, in the
     * calendar_dates.txt file.
     */
    public boolean isAvailableOnSunday() {
        return this.sunday;
    }

    /**
     * start_date Required The start_date field contains the start date for the service. The
     * start_date field's value should be in YYYYMMDD format.
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * end_date Required The end_date field contains the end date for the service. This date is
     * included in the service interval. The end_date field's value should be in YYYYMMDD format.
     */
    public Date getEndDate() {
        return this.endDate;
    }

}
