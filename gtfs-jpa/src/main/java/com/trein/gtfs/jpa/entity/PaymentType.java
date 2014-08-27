package com.trein.gtfs.jpa.entity;

/**
 * The payment_method field indicates when the fare must be paid. Valid
 *
 * @author trein
 */
public enum PaymentType {
    ON_BOARD(0), BEFORE_BOARD(1);

    private final int code;

    private PaymentType(int code) {
        this.code = code;
    }

    /**
     * values for this field are:
     *
     * <pre>
     * 0 - Fare is paid on board.
     * 1 - Fare must be paid before boarding.
     * </pre>
     */
    public int getCode() {
        return this.code;
    }

    public static PaymentType fromCode(Integer code) {
        if (code != null) {
            for (PaymentType e : PaymentType.values()) {
                if (e.getCode() == code.intValue()) { return e; }
            }
        }
        return ON_BOARD;
    }

}
