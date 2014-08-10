package com.trein.gtfs.jpa.entities;

/**
 * drop_off_type / pick_up_type Optional The drop_off_type / pick_up_type field indicates whether
 * passengers are dropped off / pick up at a stop as part of the normal schedule or whether a drop
 * off at the stop is not available. This field also allows the transit agency to indicate that
 * passengers must call the agency or notify the driver to arrange a drop off / pick up at a
 * particular stop.
 *
 * @author trein
 */
public enum AvailabilityType {
    REGULAR(0), NOT_AVAILABLE(1), MUST_PHONE_AGENCY(2), MUST_COORDINATE_WITH_DRIVER(3);
    
    private final int code;
    
    private AvailabilityType(int code) {
        this.code = code;
    }
    
    /**
     * Valid values for this field are:
     *
     * <pre>
     *     0 - Regularly scheduled drop off / pick up
     *     1 - No drop off / pick up available
     *     2 - Must phone agency to arrange drop off / pick up
     *     3 - Must coordinate with driver to arrange drop off / pick up
     * </pre>
     *
     * The default value for this field is 0.
     *
     * @return code corresponding to drop off / pick up type.
     */
    public int getCode() {
        return this.code;
    }
    
    public static AvailabilityType fromCode(Integer code) {
        if (code != null) {
            for (AvailabilityType e : AvailabilityType.values()) {
                if (e.getCode() == code.intValue()) { return e; }
            }
        }
        return REGULAR;
    }
    
}
