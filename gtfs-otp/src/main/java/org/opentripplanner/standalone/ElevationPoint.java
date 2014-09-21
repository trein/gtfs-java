package org.opentripplanner.standalone;

public class ElevationPoint implements Comparable<ElevationPoint> {
    public double distanceAlongShape, ele;
    
    public ElevationPoint(double distance, double ele) {
        this.distanceAlongShape = distance;
        this.ele = ele;
    }
    
    public ElevationPoint fromBack(double length) {
        return new ElevationPoint(length - this.distanceAlongShape, this.ele);
    }
    
    @Override
    public int compareTo(ElevationPoint arg0) {
        return (int) Math.signum(this.distanceAlongShape - arg0.distanceAlongShape);
    }

    @Override
    public String toString() {
        return "ElevationPoint(" + this.distanceAlongShape + ", " + this.ele + ")";
    }
    
}
