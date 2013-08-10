package com.trein.gtfs.vo;

/**
 * Rules for making connections at transfer points between routes.<br>
 * <br>
 * Trip planners normally calculate transfer points based on the relative proximity of stops in each
 * route. For potentially ambiguous stop pairs, or transfers where you want to specify a particular
 * choice, use transfers.txt to define additional rules for making connections between routes.
 * 
 * @author trein
 */
public class Transfer {
    
    private long fromStopId;
    private long toStopId;
    private int transferType;
    private long minTransferTimeSecs;
    
    /**
     * from_stop_id Required The from_stop_id field contains a stop ID that identifies a stop or
     * station where a connection between routes begins. Stop IDs are referenced from the stops.txt
     * file. If the stop ID refers to a station that contains multiple stops, this transfer rule
     * applies to all stops in that station.
     */
    public long getFromStopId() {
	return this.fromStopId;
    }
    
    /**
     * to_stop_id Required The to_stop_id field contains a stop ID that identifies a stop or station
     * where a connection between routes ends. Stop IDs are referenced from the stops.txt file. If
     * the stop ID refers to a station that contains multiple stops, this transfer rule applies to
     * all stops in that station.
     */
    public long getToStopId() {
	return this.toStopId;
    }
    
    /**
     * transfer_type Required The transfer_type field specifies the type of connection for the
     * specified (from_stop_id, to_stop_id) pair. Valid values for this field are:
     * 
     * <pre>
     * 0 or (empty) - This is a recommended transfer point between two routes.
     * 1 - This is a timed transfer point between two routes. The departing vehicle is expected to wait for the arriving one, with sufficient time for a passenger to transfer between routes.
     * 2 - This transfer requires a minimum amount of time between arrival and departure to ensure a connection. The time required to transfer is specified by min_transfer_time.
     * 3 - Transfers are not possible between routes at this location.
     * </pre>
     */
    public int getTransferType() {
	return this.transferType;
    }
    
    /**
     * min_transfer_time Optional When a connection between routes requires an amount of time
     * between arrival and departure (transfer_type=2), the min_transfer_time field defines the
     * amount of time that must be available in an itinerary to permit a transfer between routes at
     * these stops. The min_transfer_time must be sufficient to permit a typical rider to move
     * between the two stops, including buffer time to allow for schedule variance on each route.
     * The min_transfer_time value must be entered in seconds, and must be a non-negative integer.
     */
    public long getMinTransferTimeSecs() {
	return this.minTransferTimeSecs;
    }
    
}
