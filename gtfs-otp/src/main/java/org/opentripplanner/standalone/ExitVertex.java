package org.opentripplanner.standalone;


public class ExitVertex extends IntersectionVertex {

    private static final long serialVersionUID = -1403959315797898914L;
    private String exitName;

    public ExitVertex(Graph g, String label, double x, double y) {
        super(g, label, x, y);
    }
    
    public String getExitName() {
        return this.exitName;
    }
    
    public void setExitName(String exitName) {
        this.exitName = exitName;
    }
    
    @Override
    public String toString() {
        return "ExitVertex(" + super.toString() + ")";
    }
}
