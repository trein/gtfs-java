package com.trein.gtfs.otp.building.graph.api;

import java.io.Serializable;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType
public class TranslatedString implements Serializable {
    private static final long serialVersionUID = 2163930399727941628L;
    
    @XmlElement
    @XmlJavaTypeAdapter(MapAdapter.class)
    public TreeMap<String, String> translations = new TreeMap<String, String>();
    
    public TranslatedString(String language, String note) {
        this.translations.put(language.intern(), note);
    }
    
    public TranslatedString() {
    }
    
    public TranslatedString(String v) {
        this(Alert.defaultLanguage, v);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TranslatedString)) { return false; }
        TranslatedString tso = (TranslatedString) o;
        return tso.translations.equals(this.translations);
    }
    
    @Override
    public int hashCode() {
        return this.translations.hashCode() + 1;
    }
    
    public void addTranslation(String language, String note) {
        this.translations.put(language.intern(), note);
    }
    
    // fixme: need to get en-US when requested language is "en"
    public String getTranslation(String language) {
        return this.translations.get(language);
    }
    
    public String getSomeTranslation() {
        if (this.translations.isEmpty()) { return null; }
        return this.translations.values().iterator().next();
    }
    
    @Override
    public String toString() {
        return "TranslateString(" + getSomeTranslation() + ")";
    }
}
