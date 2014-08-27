package com.trein.gtfs.jpa.entity;

/**
 * The transfer_type field specifies the type of connection for the specified (from_stop_id,
 * to_stop_id) pair.
 *
 * @author trein
 */
public enum TransferType {
    RECOMMENDED(0), TRANSFER_WILL_WAIT(1), REQUIRE_MIN_TIME(2), NOT_POSSIBLE(3);

    private final int code;

    private TransferType(int code) {
        this.code = code;
    }

    /**
     * Valid values for this field are:
     *
     * <pre>
     * 0 or (empty) - This is a recommended transfer point between two routes.
     * 1 - This is a timed transfer point between two routes. The departing vehicle is expected
     * to wait for the arriving one, with sufficient time for a passenger to transfer between routes.
     * 2 - This transfer requires a minimum amount of time between arrival and departure to ensure
     * a connection. The time required to transfer is specified by min_transfer_time.
     * 3 - Transfers are not possible between routes at this location.
     * </pre>
     *
     * @return code corresponding to drop off / pick up type.
     */
    public int getCode() {
        return this.code;
    }

    public static TransferType fromCode(Integer code) {
        if (code != null) {
            for (TransferType e : TransferType.values()) {
                if (e.getCode() == code.intValue()) { return e; }
            }
        }
        return NOT_POSSIBLE;
    }
}
