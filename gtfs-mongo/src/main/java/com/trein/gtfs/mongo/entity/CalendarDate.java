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
 * Exceptions for the service IDs defined in the calendar.txt file. If calendar_dates.txt includes
 * ALL dates of service, this file may be specified instead of calendar.txt.<br>
 * <br>
 * The calendar_dates table allows you to explicitly activate or disable service IDs by date. You
 * can use it in two ways. Recommended: Use calendar_dates.txt in conjunction with calendar.txt,
 * where calendar_dates.txt defines any exceptions to the default service categories defined in the
 * calendar.txt file. If your service is generally regular, with a few changes on explicit dates
 * (for example, to accomodate special event services, or a school schedule), this is a good
 * approach. Alternate: Omit calendar.txt, and include ALL dates of service in calendar_dates.txt.
 * If your schedule varies most days of the month, or you want to programmatically output service
 * dates without specifying a normal weekly schedule, this approach may be preferable.
 *
 * @author trein
 */
@Document
public class CalendarDate {

    @Id
    private ObjectId id;

    @Indexed
    private String serviceId;

    private Date date;
    private ExceptionType exceptionType;

    CalendarDate() {
    }

    public CalendarDate(String serviceId, Date date, ExceptionType exceptionType) {
        this.serviceId = serviceId;
        this.date = date;
        this.exceptionType = exceptionType;
    }

    public ObjectId getId() {
        return this.id;
    }

    /**
     * service_id Required The service_id contains an ID that uniquely identifies a set of dates
     * when a service exception is available for one or more routes. Each (service_id, date) pair
     * can only appear once in calendar_dates.txt. If the a service_id value appears in both the
     * calendar.txt and calendar_dates.txt files, the information in calendar_dates.txt modifies the
     * service information specified in calendar.txt. This field is referenced by the trips.txt
     * file.
     *
     * @return service's id related to current calendar date.
     */
    public String getServiceId() {
        return this.serviceId;
    }

    /**
     * date Required The date field specifies a particular date when service availability is
     * different than the norm. You can use the exception_type field to indicate whether service is
     * available on the specified date. The date field's value should be in YYYYMMDD format.
     *
     * @return date identifying current entry.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * exception_type Required The exception_type indicates whether service is available on the date
     * specified in the date field.
     *
     * @return service exception's type for current date.
     * @see ExceptionType refer to documentation for more clarification.
     */
    public ExceptionType getExceptionType() {
        return this.exceptionType;
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
