package com.trein.gtfs.orm.entities;

/**
 * direction_id Optional The direction_id field contains a binary value that indicates the direction
 * of travel for a trip. Use this field to distinguish between bi-directional trips with the same
 * route_id. This field is not used in routing; it provides a way to separate trips by direction
 * when publishing time tables. You can specify names for each direction with the trip_headsign
 * field.
 *
 * @author trein
 */
public enum DirectionType {
    INBOUND(0), OUTBOUND(1);

    private final int code;

    private DirectionType(int code) {
        this.code = code;
    }

    /**
     * It provides a way to separate trips by direction when publishing time tables. You can specify
     * names for each direction with the trip_headsign field.
     *
     * <pre>
     *     0 - travel in one direction (e.g. outbound travel)
     *     1 - travel in the opposite direction (e.g. inbound travel)
     * </pre>
     *
     * @return code corresponding to current direction.
     */
    public int getCode() {
        return this.code;
    }
    
    public static DirectionType valueOf(int code) {
        for (DirectionType e : DirectionType.values()) {
            if (e.getCode() == code) { return e; }
        }
        return INBOUND;
    }

}
