package com.trein.gtfs.jpa.entity;

/**
 * The wheelchair_boarding field identifies whether wheelchair boardings are possible from the
 * specified stop / station, trips or routes.
 *
 * @author trein
 */
public enum WheelchairType {
    NO_INFO(0), AVAILABLE_AT_SOME_VEHICLES(1), NOT_AVAILABLE(2);
    
    private final int code;
    
    private WheelchairType(int code) {
        this.code = code;
    }
    
    /**
     * Corresponding code to accessible service available.
     *
     * @return code for the given accessible service available.
     */
    public int getCode() {
        return this.code;
    }
    
    public static WheelchairType fromCode(Integer code) {
        if (code != null) {
            for (WheelchairType e : WheelchairType.values()) {
                if (e.getCode() == code.intValue()) { return e; }
            }
        }
        return NO_INFO;
    }
    
}
