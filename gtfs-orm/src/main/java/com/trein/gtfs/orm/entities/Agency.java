package com.trein.gtfs.orm.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * One or more transit agencies that provide the data in this feed. Hibernate disables insert
 * batching at the JDBC level transparently if you use an identity identifier generator.
 *
 * @author trein
 */
@Entity(name = "agencies")
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Agency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "agency_id", nullable = false)
    private String agencyId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "url", nullable = false)
    private String url;
    
    @Column(name = "timezone", nullable = false)
    private String timezone;
    
    @Column(name = "lang", nullable = true)
    private String lang;
    
    @Column(name = "phone", nullable = true)
    private String phone;
    
    @Column(name = "fare_url", nullable = true)
    private String fareUrl;

    Agency() {
    }
    
    public Agency(String agencyId, String name, String url, String timezone, String lang, String phone, String fareUrl) {
        this.agencyId = agencyId;
        this.name = name;
        this.url = url;
        this.timezone = timezone;
        this.lang = lang;
        this.phone = phone;
        this.fareUrl = fareUrl;
    }

    public long getId() {
        return this.id;
    }
    
    /**
     * agency_id Optional: The agency_id field is an ID that uniquely identifies a transit agency. A
     * transit feed may represent data from more than one agency. The agency_id is dataset unique.
     * This field is optional for transit feeds that only contain data for a single agency.
     *
     * @return current agency's id.
     */
    public String getAgencyId() {
        return this.agencyId;
    }
    
    /**
     * agency_name Required: The agency_name field contains the full name of the transit agency.
     * Google Maps will display this name.
     *
     * @return current agency's name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * agency_url Required: The agency_url field contains the URL of the transit agency. The value
     * must be a fully qualified URL that includes http:// or https://, and any special characters
     * in the URL must be correctly escaped. See
     * http://www.w3.org/Addressing/URL/4_URI_Recommentations.html for a description of how to
     * create fully qualified URL values.
     *
     * @return current agency's url.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * agency_timezone Required: The agency_timezone field contains the timezone where the transit
     * agency is located. Timezone names never contain the space character but may contain an
     * underscore. Please refer to http://en.wikipedia.org/wiki/List_of_tz_zones for a list of valid
     * values. If multiple agencies are specified in the feed, each must have the same
     * agency_timezone.
     *
     * @return current agency's timezone.
     */
    public String getTimezone() {
        return this.timezone;
    }
    
    /***
     * agency_lang Optional: The agency_lang field contains a two-letter ISO 639-1 code for the
     * primary language used by this transit agency. The language code is case-insensitive (both en
     * and EN are accepted). This setting defines capitalization rules and other language-specific
     * settings for all text contained in this transit agency's feed. Please refer to
     * http://www.loc.gov/standards/iso639-2/php/code_list.php for a list of valid values.
     *
     * @return current agency's language.
     */
    public String getLang() {
        return this.lang;
    }
    
    /**
     * agency_phone Optional: The agency_phone field contains a single voice telephone number for
     * the specified agency. This field is a string value that presents the telephone number as
     * typical for the agency's service area. It can and should contain punctuation marks to group
     * the digits of the number. Dialable text (for example, TriMet's "503-238-RIDE") is permitted,
     * but the field must not contain any other descriptive text.
     *
     * @return current agency's main telephone.
     */
    public String getPhone() {
        return this.phone;
    }
    
    /**
     * agency_fare_url Optional: The agency_fare_url specifies the URL of a web page that allows a
     * rider to purchase tickets or other fare instruments for that agency online. The value must be
     * a fully qualified URL that includes http:// or https://, and any special characters in the
     * URL must be correctly escaped. See
     * http://www.w3.org/Addressing/URL/4_URI_Recommentations.html for a description of how to
     * create fully qualified URL values.
     *
     * @return current agency's fare url.
     */
    public String getFareUrl() {
        return this.fareUrl;
    }
    
}
