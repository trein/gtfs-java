package com.trein.gtfs.orm.entities;

/**
 * The transfers field specifies the number of transfers permitted on this fare.
 *
 * @author trein
 */
public enum FareTransferType {
    NOT_ALLOWED(0), ONCE(1), TWICE(2), UNLIMITED(-1);

    private final int code;

    private FareTransferType(int code) {
        this.code = code;
    }

    /**
     * Valid values for this field are:
     *
     * <pre>
     * 0 - No transfers permitted on this fare.
     * 1 - Passenger may transfer once.
     * 2 - Passenger may transfer twice.
     * (empty) - If this field is empty, unlimited transfers are permitted.
     * </pre>
     *
     * @return code corresponding to drop off / pick up type.
     */
    public int getCode() {
        return this.code;
    }

    public static FareTransferType fromCode(String code) {
        int internalCode = -1;
        if ((code != null) && !"".equals(code)) {
            internalCode = Integer.parseInt(code);
        }
        for (FareTransferType e : FareTransferType.values()) {
            if (e.getCode() == internalCode) { return e; }
        }
        return NOT_ALLOWED;
    }
}
