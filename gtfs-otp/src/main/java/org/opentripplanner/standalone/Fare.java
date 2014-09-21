package org.opentripplanner.standalone;

import java.util.HashMap;

/**
 * <p>
 * Fare is a set of fares for different classes of users.
 * </p>
 */
public class Fare {
    
    public static enum FareType {
        regular, student, senior, tram, special
    }
    
    /**
     * A mapping from {@link FareType} to {@link Money}.
     */
    public HashMap<FareType, Money> fare;
    
    public Fare() {
        this.fare = new HashMap<FareType, Money>();
    }
    
    public void addFare(FareType fareType, WrappedCurrency currency, int cents) {
        this.fare.put(fareType, new Money(currency, cents));
    }
    
    public Money getFare(FareType type) {
        return this.fare.get(type);
    }
    
    public void addCost(int surcharge) {
        for (Money cost : this.fare.values()) {
            int cents = cost.getCents();
            cost.setCents(cents + surcharge);
        }
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("Fare(");
        for (FareType type : this.fare.keySet()) {
            Money cost = this.fare.get(type);
            buffer.append("[");
            buffer.append(type.toString());
            buffer.append(":");
            buffer.append(cost.toString());
            buffer.append("], ");
        }
        buffer.append(")");
        return buffer.toString();
    }
}
