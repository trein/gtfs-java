package com.trein.gtfs.vo;

import com.trein.gtfs.csv.annotations.GtfsColumn;
import com.trein.gtfs.csv.annotations.GtfsFile;

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
@GtfsFile("calendar_dates.txt")
public class GtfsCalendarDate {
    
    @GtfsColumn(column = "service_id")
    private String serviceId;
    
    @GtfsColumn(column = "date")
    private String date;
    
    @GtfsColumn(column = "exception_type")
    private Integer exceptionType;
    
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
    public String getDate() {
	return this.date;
    }
    
    /**
     * exception_type Required The exception_type indicates whether service is available on the date
     * specified in the date field.
     * 
     * <pre>
     *     A value of 1 indicates that service has been added for the specified date.
     *     A value of 2 indicates that service has been removed for the specified date.
     * </pre>
     * 
     * For example, suppose a route has one set of trips available on holidays and another set of
     * trips available on all other days. You could have one service_id that corresponds to the
     * regular service schedule and another service_id that corresponds to the holiday schedule. For
     * a particular holiday, you would use the calendar_dates.txt file to add the holiday to the
     * holiday service_id and to remove the holiday from the regular service_id schedule.
     * 
     * @return service exception's type for current date.
     */
    public Integer getExceptionType() {
	return this.exceptionType;
    }
    
}
