package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Alert implements Serializable {
    private static final long serialVersionUID = 8305126586053909836L;
    
    public static final String defaultLanguage = "en";
    
    @XmlElement
    public TranslatedString alertHeaderText;
    
    @XmlElement
    public TranslatedString alertDescriptionText;
    
    @XmlElement
    public TranslatedString alertUrl;
    
    // null means unknown
    @XmlElement
    public Date effectiveStartDate;
    
    public static HashSet<Alert> newSimpleAlertSet(String text) {
        Alert note = createSimpleAlerts(text);
        HashSet<Alert> notes = new HashSet<Alert>(1);
        notes.add(note);
        return notes;
    }
    
    public static Alert createSimpleAlerts(String text) {
        Alert note = new Alert();
        note.alertHeaderText = new TranslatedString();
        note.alertHeaderText.addTranslation(defaultLanguage, text);
        return note;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alert)) { return false; }
        Alert ao = (Alert) o;
        if (this.alertDescriptionText == null) {
            if (ao.alertDescriptionText != null) { return false; }
        } else {
            if (!this.alertDescriptionText.equals(ao.alertDescriptionText)) { return false; }
        }
        if (this.alertHeaderText == null) {
            if (ao.alertHeaderText != null) { return false; }
        } else {
            if (!this.alertHeaderText.equals(ao.alertHeaderText)) { return false; }
        }
        if (this.alertUrl == null) {
            return ao.alertUrl == null;
        } else {
            return this.alertUrl.equals(ao.alertUrl);
        }
    }
    
    @Override
    public int hashCode() {
        return (this.alertDescriptionText == null ? 0 : this.alertDescriptionText.hashCode())
                + (this.alertHeaderText == null ? 0 : this.alertHeaderText.hashCode())
                + (this.alertUrl == null ? 0 : this.alertUrl.hashCode());
    }
}
