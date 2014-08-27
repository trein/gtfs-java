package com.trein.gtfs.mongo.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Dates for service IDs using a weekly schedule. Specify when service starts and ends, as well as
 * days of the week where service is available.
 *
 * @author trein
 */
@Document
public class Calendar {

    @Id
    private ObjectId id;

    @Indexed
    private String serviceId;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    private Date startDate;
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

    public ObjectId getId() {
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

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).build();
    }

}
