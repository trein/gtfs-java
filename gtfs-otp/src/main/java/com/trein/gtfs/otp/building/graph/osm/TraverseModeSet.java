package com.trein.gtfs.otp.building.graph.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trein.gtfs.otp.building.graph.api.TraverseMode;

/**
 * A set of traverse modes -- typically, one non-transit mode (walking, biking, car) and zero or
 * more transit modes (bus, tram, etc). This class allows efficiently adding or removing modes from
 * a set.
 * 
 * @author novalis
 */
public class TraverseModeSet implements Cloneable, Serializable {
    
    private static final long serialVersionUID = -1640048158419762255L;
    
    private static final int MODE_BICYCLE = 1;
    
    private static final int MODE_WALK = 2;
    
    private static final int MODE_CAR = 4;
    
    private static final int MODE_BUS = 16;
    
    private static final int MODE_TRAM = 32;
    
    private static final int MODE_SUBWAY = 64;
    
    private static final int MODE_RAIL = 128;
    
    private static final int MODE_FERRY = 256;
    
    private static final int MODE_CABLE_CAR = 512;
    
    private static final int MODE_GONDOLA = 1024;
    
    private static final int MODE_FUNICULAR = 2048;

    private static final int MODE_CUSTOM_MOTOR_VEHICLE = 4096;
    
    private static final int MODE_TRAINISH = MODE_TRAM | MODE_RAIL | MODE_SUBWAY | MODE_FUNICULAR | MODE_GONDOLA;
    
    private static final int MODE_BUSISH = MODE_CABLE_CAR | MODE_BUS;
    
    private static final int MODE_TRANSIT = MODE_TRAINISH | MODE_BUSISH | MODE_FERRY;

    private static final int MODE_DRIVING = MODE_CAR | MODE_CUSTOM_MOTOR_VEHICLE;

    private static final int MODE_ALL = MODE_TRANSIT | MODE_DRIVING | MODE_WALK | MODE_BICYCLE;
    
    private int modes = 0;
    
    public TraverseModeSet(String modelist) {
        this.modes = 0;
        for (String modeStr : modelist.split(",")) {
            if (modeStr.length() == 0) {
                continue;
            }
            setMode(TraverseMode.valueOf(modeStr), true);
        }
        
    }
    
    public TraverseModeSet(TraverseMode... modes) {
        for (TraverseMode mode : modes) {
            this.modes |= getMaskForMode(mode);
        }
    }

    /**
     * Returns a mode set containing all modes.
     *
     * @return
     */
    public static TraverseModeSet allModes() {
        TraverseModeSet modes = new TraverseModeSet();
        modes.modes = MODE_ALL;
        return modes;
    }
    
    private final int getMaskForMode(TraverseMode mode) {
        switch (mode) {
            case BICYCLE:
                return MODE_BICYCLE;
            case WALK:
                return MODE_WALK;
            case CAR:
                return MODE_CAR;
            case CUSTOM_MOTOR_VEHICLE:
                return MODE_CUSTOM_MOTOR_VEHICLE;
            case BUS:
                return MODE_BUS;
            case TRAM:
                return MODE_TRAM;
            case CABLE_CAR:
                return MODE_CABLE_CAR;
            case GONDOLA:
                return MODE_GONDOLA;
            case FERRY:
                return MODE_FERRY;
            case FUNICULAR:
                return MODE_FUNICULAR;
            case SUBWAY:
                return MODE_SUBWAY;
            case RAIL:
                return MODE_RAIL;
            case TRAINISH:
                return MODE_TRAINISH;
            case BUSISH:
                return MODE_BUSISH;
            case TRANSIT:
                return MODE_TRANSIT;
        }
        return 0;
    }
    
    public TraverseModeSet(Collection<TraverseMode> modeList) {
        this(modeList.toArray(new TraverseMode[0]));
    }

    public int getMask() {
        return this.modes;
    }
    
    public void setMode(TraverseMode mode, boolean value) {
        int mask = getMaskForMode(mode);
        if (value) {
            this.modes |= mask;
        } else {
            this.modes &= ~mask;
        }
    }
    
    public boolean getBicycle() {
        return (this.modes & MODE_BICYCLE) != 0;
    }
    
    public boolean getWalk() {
        return (this.modes & MODE_WALK) != 0;
    }
    
    public boolean getCar() {
        return (this.modes & MODE_CAR) != 0;
    }

    public boolean getCustomMotorVehicle() {
        return (this.modes & MODE_CUSTOM_MOTOR_VEHICLE) != 0;
    }

    public boolean getDriving() {
        return (this.modes & MODE_DRIVING) != 0;
    }
    
    public boolean getTram() {
        return (this.modes & MODE_TRAM) != 0;
    }

    public boolean getTrainish() {
        return (this.modes & MODE_TRAINISH) != 0;
    }

    public boolean getBusish() {
        return (this.modes & MODE_BUSISH) != 0;
    }

    public boolean getBus() {
        return (this.modes & MODE_BUS) != 0;
    }

    public boolean getGondola() {
        return (this.modes & MODE_GONDOLA) != 0;
    }

    public boolean getFerry() {
        return (this.modes & MODE_FERRY) != 0;
    }

    public boolean getCableCar() {
        return (this.modes & MODE_CABLE_CAR) != 0;
    }
    
    public boolean getFunicular() {
        return (this.modes & MODE_FUNICULAR) != 0;
    }

    public boolean getRail() {
        return (this.modes & MODE_RAIL) != 0;
    }

    public boolean getSubway() {
        return (this.modes & MODE_SUBWAY) != 0;
    }

    public void setBicycle(boolean bicycle) {
        if (bicycle) {
            this.modes |= MODE_BICYCLE;
        } else {
            this.modes &= ~MODE_BICYCLE;
        }
    }
    
    public void setWalk(boolean walk) {
        if (walk) {
            this.modes |= MODE_WALK;
        } else {
            this.modes &= ~MODE_WALK;
        }
    }
    
    public void setCar(boolean car) {
        if (car) {
            this.modes |= MODE_CAR;
        } else {
            this.modes &= ~MODE_CAR;
        }
    }

    public void setCustomMotorVehicle(boolean cmv) {
        if (cmv) {
            this.modes |= MODE_CUSTOM_MOTOR_VEHICLE;
        } else {
            this.modes &= ~MODE_CUSTOM_MOTOR_VEHICLE;
        }
    }

    public void setDriving(boolean driving) {
        if (driving) {
            this.modes |= MODE_DRIVING;
        } else {
            this.modes &= ~MODE_DRIVING;
        }
    }
    
    public void setTram(boolean tram) {
        if (tram) {
            this.modes |= MODE_TRAM;
        } else {
            this.modes &= ~MODE_TRAM;
        }
    }
    
    public void setTrainish(boolean trainish) {
        if (trainish) {
            this.modes |= MODE_TRAINISH;
        } else {
            this.modes &= ~MODE_TRAINISH;
        }
    }

    public void setBus(boolean bus) {
        if (bus) {
            this.modes |= MODE_BUS;
        } else {
            this.modes &= ~MODE_BUS;
        }
    }
    
    public void setBusish(boolean busish) {
        if (busish) {
            this.modes |= MODE_BUSISH;
        } else {
            this.modes &= ~MODE_BUSISH;
        }
    }

    public void setFerry(boolean ferry) {
        if (ferry) {
            this.modes |= MODE_FERRY;
        } else {
            this.modes &= ~MODE_FERRY;
        }
    }
    
    public void setCableCar(boolean cableCar) {
        if (cableCar) {
            this.modes |= MODE_CABLE_CAR;
        } else {
            this.modes &= ~MODE_CABLE_CAR;
        }
    }
    
    public void setGondola(boolean gondola) {
        if (gondola) {
            this.modes |= MODE_GONDOLA;
        } else {
            this.modes &= ~MODE_GONDOLA;
        }
    }
    
    public void setFunicular(boolean funicular) {
        if (funicular) {
            this.modes |= MODE_FUNICULAR;
        } else {
            this.modes &= ~MODE_FUNICULAR;
        }
    }
    
    public void setSubway(boolean subway) {
        if (subway) {
            this.modes |= MODE_SUBWAY;
        } else {
            this.modes &= ~MODE_SUBWAY;
        }
    }

    public void setRail(boolean rail) {
        if (rail) {
            this.modes |= MODE_RAIL;
        } else {
            this.modes &= ~MODE_RAIL;
        }
    }
    
    /** Returns true if the trip may use some transit mode */
    public boolean isTransit() {
        return (this.modes & (MODE_TRANSIT)) != 0;
    }
    
    public void setTransit(boolean transit) {
        if (transit) {
            this.modes |= MODE_TRANSIT;
        } else {
            this.modes &= ~MODE_TRANSIT;
        }
    }
    
    /** Returns a TraverseModeSet containing only the non-transit modes set. */
    public TraverseModeSet getNonTransitSet() {
        TraverseModeSet retval = new TraverseModeSet();
        retval.modes = this.modes;
        retval.setTransit(false);
        return retval;
    }
    
    /** Returns true if any the trip may use some train-like (train, subway, tram) mode */
    public boolean getTraininsh() {
        return (this.modes & (MODE_TRAINISH)) != 0;
    }
    
    public List<TraverseMode> getModes() {
        ArrayList<TraverseMode> modeList = new ArrayList<TraverseMode>();
        for (TraverseMode mode : TraverseMode.values()) {
            if ((this.modes & getMaskForMode(mode)) != 0) {
                modeList.add(mode);
            }
        }
        return modeList;
    }
    
    public boolean isValid() {
        return this.modes != 0;
    }
    
    public boolean contains(TraverseMode mode) {
        return (this.modes & getMaskForMode(mode)) != 0;
    }
    
    public boolean get(int modeMask) {
        return (this.modes & modeMask) != 0;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (TraverseMode mode : TraverseMode.values()) {
            int mask = getMaskForMode(mode);
            if ((mask != 0) && ((this.modes & mask) == mask)) {
                if (out.length() != 0) {
                    out.append(", ");
                }
                out.append(mode);
            }
        }
        return "TraverseMode (" + out + ")";
    }
    
    public String getAsStr() {
        String retVal = null;
        for (TraverseMode m : getModes()) {
            if (retVal == null) {
                retVal = "";
            } else {
                retVal += ", ";
            }
            retVal += m;
        }
        return retVal;
    }
    
    @Override
    public TraverseModeSet clone() {
        try {
            return (TraverseModeSet) super.clone();
        } catch (CloneNotSupportedException e) {
            /* this will never happen since our super is the cloneable object */
            throw new RuntimeException(e);
        }
    }

    /**
     * Clear the mode set so that no modes are included.
     */
    public void clear() {
        this.modes = 0;
    }
    
    @Override
    public int hashCode() {
        return this.modes;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof TraverseModeSet) { return this.modes == ((TraverseModeSet) other).modes; }
        return false;
    }
    
}
