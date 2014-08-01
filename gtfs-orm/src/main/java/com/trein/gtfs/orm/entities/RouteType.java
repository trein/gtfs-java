package com.trein.gtfs.orm.entities;

/**
 * route_type Required The route_type field describes the type of transportation used on a route.
 * See a Google Maps screenshot highlighting the route_type.
 *
 * @author trein
 */
public enum RouteType {
    TRAM_CAR(0), SUBWAY(1), RAIL(2), BUS(3), FERRY(4), CABLE_CAR(5), GONDOLA(6), FUNICULAR(7);
    
    private final int code;
    
    private RouteType(int code) {
        this.code = code;
    }
    
    /**
     * Valid values for this field are:
     *
     * <pre>
     * 0 - Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
     * 1 - Subway, Metro. Any underground rail system within a metropolitan area.
     * 2 - Rail. Used for intercity or long-distance travel.
     * 3 - Bus. Used for short- and long-distance bus routes.
     * 4 - Ferry. Used for short- and long-distance boat service.
     * 5 - Cable car. Used for street-level cable cars where the cable runs beneath the car.
     * 6 - Gondola, Suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.
     * 7 - Funicular. Any rail system designed for steep inclines.
     * </pre>
     *
     * @return code corresponding the type of the route.
     */
    public int getCode() {
        return this.code;
    }

    public static RouteType valueOf(int code) {
        for (RouteType e : RouteType.values()) {
            if (e.getCode() == code) { return e; }
        }
        return RAIL;
    }
    
}
