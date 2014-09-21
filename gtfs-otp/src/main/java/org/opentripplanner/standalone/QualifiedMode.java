package org.opentripplanner.standalone;

import java.security.InvalidParameterException;
import java.util.Set;

import com.beust.jcommander.internal.Sets;

public class QualifiedMode {
    
    public final TraverseMode mode;
    public final Set<Qualifier> qualifiers = Sets.newHashSet();

    public QualifiedMode(String qMode) {
        String[] elements = qMode.split("_");
        this.mode = TraverseMode.valueOf(elements[0].trim());
        if (this.mode == null) { throw new InvalidParameterException(); }
        for (int i = 1; i < elements.length; i++) {
            Qualifier q = Qualifier.valueOf(elements[i].trim());
            if (q == null) {
                throw new InvalidParameterException();
            } else {
                this.qualifiers.add(q);
            }
        }
    }
    
}

enum Qualifier {
    RENT, HAVE, PARK, KEEP
}
