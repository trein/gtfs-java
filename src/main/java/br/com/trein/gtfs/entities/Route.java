package br.com.trein.gtfs.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Transit routes. A route is a group of trips that are displayed to riders as a single service.
 * 
 * @author trein
 */
@Entity(name = "routes")
public class Route {
    
    @Id
    private long id;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "id", nullable = false, updatable = false)
    private Agency agency;
    
    @Column(name = "short_name")
    private String shortName;
    
    @Column(name = "long_name")
    private String longName;
    
    @Column(name = "desc")
    private String desc;
    
    @Column(name = "route_type")
    private RouteType type;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "hex_path_color")
    private int hexPathColor;
    
    @Column(name = "hex_text_color")
    private int hexTextColor;
    
    /**
     * route_id Required The route_id field contains an ID that uniquely identifies a route. The
     * route_id is dataset unique.
     * 
     * @return current route's id.
     */
    public long getId() {
	return this.id;
    }
    
    /**
     * agency_id Optional The agency_id field defines an agency for the specified route. This value
     * is referenced from the agency.txt file. Use this field when you are providing data for routes
     * from more than one agency.
     * 
     * @return agency related to this given route.
     */
    public Agency getAgency() {
	return this.agency;
    }
    
    /**
     * route_short_name Required The route_short_name contains the short name of a route. This will
     * often be a short, abstract identifier like "32", "100X", or "Green" that riders use to
     * identify a route, but which doesn't give any indication of what places the route serves. At
     * least one of route_short_name or route_long_name must be specified, or potentially both if
     * appropriate. If the route does not have a short name, please specify a route_long_name and
     * use an empty string as the value for this field. See a Google Maps screenshot highlighting
     * the route_short_name.
     * 
     * @return current route's short name.
     */
    public String getShortName() {
	return this.shortName;
    }
    
    /**
     * route_long_name Required The route_long_name contains the full name of a route. This name is
     * generally more descriptive than the route_short_name and will often include the route's
     * destination or stop. At least one of route_short_name or route_long_name must be specified,
     * or potentially both if appropriate. If the route does not have a long name, please specify a
     * route_short_name and use an empty string as the value for this field. See a Google Maps
     * screenshot highlighting the route_long_name.
     * 
     * @return current route's long name.
     */
    public String getLongName() {
	return this.longName;
    }
    
    /**
     * route_desc Optional The route_desc field contains a description of a route. Please provide
     * useful, quality information. Do not simply duplicate the name of the route. For example,
     * 
     * <pre>
     * A trains operate between Inwood-207 St, Manhattan and Far Rockaway-Mott Avenue, Queens at all times.
     * Also from about 6AM until about midnight, additional A trains operate between Inwood-207 St and
     * Lefferts Boulevard (trains typically alternate between Lefferts Blvd and Far Rockaway).
     * </pre>
     * 
     * @return current route's description.
     */
    public String getDesc() {
	return this.desc;
    }
    
    /**
     * route_type Required The route_type field describes the type of transportation used on a
     * route.
     * 
     * @return current route's type.
     * @see RouteType refer to documentation for more details about its semantics.
     */
    public RouteType getType() {
	return this.type;
    }
    
    /**
     * route_url Optional The route_url field contains the URL of a web page about that particular
     * route. This should be different from the agency_url. The value must be a fully qualified URL
     * that includes http:// or https://, and any special characters in the URL must be correctly
     * escaped. See http://www.w3.org/Addressing/URL/4_URI_Recommentations.html for a description of
     * how to create fully qualified URL values.
     * 
     * @return current route's url.
     */
    public String getUrl() {
	return this.url;
    }
    
    /**
     * route_color Optional In systems that have colors assigned to routes, the route_color field
     * defines a color that corresponds to a route. The color must be provided as a six-character
     * hexadecimal number, for example, 00FFFF. If no color is specified, the default route color is
     * white (FFFFFF). The color difference between route_color and route_text_color should provide
     * sufficient contrast when viewed on a black and white screen. The W3C Techniques for
     * Accessibility Evaluation And Repair Tools document offers a useful algorithm for evaluating
     * color contrast. There are also helpful online tools for choosing contrasting colors,
     * including the snook.ca Color Contrast Check application.
     * 
     * @return current route's desired theme color.
     */
    public int getHexPathColor() {
	return this.hexPathColor;
    }
    
    /**
     * route_text_color Optional The route_text_color field can be used to specify a legible color
     * to use for text drawn against a background of route_color. The color must be provided as a
     * six-character hexadecimal number, for example, FFD700. If no color is specified, the default
     * text color is black (000000). The color difference between route_color and route_text_color
     * should provide sufficient contrast when viewed on a black and white screen.
     * 
     * @return current route's desired text color.
     */
    public int getHexTextColor() {
	return this.hexTextColor;
    }
    
}
