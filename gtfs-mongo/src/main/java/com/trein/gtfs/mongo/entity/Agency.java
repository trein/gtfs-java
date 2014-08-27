package com.trein.gtfs.mongo.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One or more transit agencies that provide the data in this feed.
 *
 * @author trein
 */
@Document
public class Agency {

    @Id
    private ObjectId id;

    @Indexed
    private String agencyId;

    private String name;
    private String url;
    private String timezone;
    private String lang;
    private String phone;
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

    public ObjectId getId() {
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

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).build();
    }

}
