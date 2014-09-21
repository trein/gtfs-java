package org.opentripplanner.standalone;

import java.util.prefs.Preferences;

/**
 * Interface for a class than can be configured through Preferences (the new API which kind of
 * replaces Properties).
 */
public interface PreferencesConfigurable {
    
    public abstract void configure(Graph graph, Preferences preferences) throws Exception;
}
