package com.trein.gtfs.vo;

import com.googlecode.jcsv.annotations.MapToNamedColumn;

/**
 * Rules for applying fare information for a transit organization's routes.<br>
 * <br>
 * The fare_rules table allows you to specify how fares in fare_attributes.txt apply to an
 * itinerary. Most fare structures use some combination of the following rules:
 * 
 * <pre>
 * <li>Fare depends on origin or destination stations.</li>
 * <li>Fare depends on which zones the itinerary passes through.</li>
 * <li>Fare depends on which route the itinerary uses.</li>
 * </pre>
 * 
 * For examples that demonstrate how to specify a fare structure with fare_rules.txt and
 * fare_attributes.txt, see FareExamples in the GoogleTransitDataFeed open source project wiki.
 * 
 * @author trein
 */
public class GtfsFareRule {
    
    @MapToNamedColumn(column = "fare_id")
    private String fareId;
    
    @MapToNamedColumn(column = "route_id", optional = true)
    private String routeId;
    
    @MapToNamedColumn(column = "origin_id", optional = true)
    private Integer originZoneId;
    
    @MapToNamedColumn(column = "destination_id", optional = true)
    private Integer destinationZoneId;
    
    @MapToNamedColumn(column = "contains_id", optional = true)
    private Integer containsId;
    
    /**
     * fare_id Required The fare_id field contains an ID that uniquely identifies a fare class. This
     * value is referenced from the fare_attributes.txt file.
     */
    public String getFareId() {
	return this.fareId;
    }
    
    /**
     * route_id Optional The route_id field associates the fare ID with a route. Route IDs are
     * referenced from the routes.txt file. If you have several routes with the same fare
     * attributes, create a row in fare_rules.txt for each route. For example, if fare class "b" is
     * valid on route "TSW" and "TSE", the fare_rules.txt file would contain these rows for the fare
     * class:
     * 
     * <pre>
     * b,TSW
     * b,TSE
     * </pre>
     */
    public String getRouteId() {
	return this.routeId;
    }
    
    /**
     * origin_id Optional The origin_id field associates the fare ID with an origin zone ID. Zone
     * IDs are referenced from the stops.txt file. If you have several origin IDs with the same fare
     * attributes, create a row in fare_rules.txt for each origin ID. For example, if fare class "b"
     * is valid for all travel originating from either zone "2" or zone "8", the fare_rules.txt file
     * would contain these rows for the fare class:
     * 
     * <pre>
     * b, , 2
     * b, , 8
     * </pre>
     */
    public Integer getOriginZoneId() {
	return this.originZoneId;
    }
    
    /**
     * destination_id Optional The destination_id field associates the fare ID with a destination
     * zone ID. Zone IDs are referenced from the stops.txt file. If you have several destination IDs
     * with the same fare attributes, create a row in fare_rules.txt for each destination ID. For
     * example, you could use the origin_ID and destination_ID fields together to specify that fare
     * class "b" is valid for travel between zones 3 and 4, and for travel between zones 3 and 5,
     * the fare_rules.txt file would contain these rows for the fare class:
     * 
     * <pre>
     * b, , 3,4
     * b, , 3,5
     * </pre>
     */
    public Integer getDestinationZoneId() {
	return this.destinationZoneId;
    }
    
    /**
     * contains_id Optional The contains_id field associates the fare ID with a zone ID, referenced
     * from the stops.txt file. The fare ID is then associated with itineraries that pass through
     * every contains_id zone. For example, if fare class "c" is associated with all travel on the
     * GRT route that passes through zones 5, 6, and 7 the fare_rules.txt would contain these rows:
     * 
     * <pre>
     * c,GRT,,,5
     * c,GRT,,,6
     * c,GRT,,,7
     * </pre>
     * 
     * Because all contains_id zones must be matched for the fare to apply, an itinerary that passes
     * through zones 5 and 6 but not zone 7 would not have fare class "c". For more detail, see
     * FareExamples in the GoogleTransitDataFeed project wiki.
     */
    public Integer getContainsId() {
	return this.containsId;
    }
    
}
