package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class TransitLocalStreetService implements Serializable {
    
    private static final long serialVersionUID = -1720564501158183582L;
    
    private final HashSet<Vertex> vertices;
    
    private HashMap<Vertex, HashMap<Vertex, int[]>> paths = new HashMap<Vertex, HashMap<Vertex, int[]>>();
    
    private HashMap<Vertex, HashMap<Vertex, T2<Double, Integer>>> costs;
    
    public TransitLocalStreetService(HashSet<Vertex> vertices, HashMap<Vertex, HashMap<Vertex, int[]>> paths,
            HashMap<Vertex, HashMap<Vertex, T2<Double, Integer>>> costs) {
        this.vertices = vertices;
        this.costs = costs;
        this.paths = paths;
    }
    
    public boolean transferrable(Vertex v) {
        return this.vertices.contains(v);
    }
    
    public HashMap<Vertex, HashMap<Vertex, int[]>> getPaths() {
        return this.paths;
    }
    
    public void setPaths(HashMap<Vertex, HashMap<Vertex, int[]>> paths) {
        this.paths = paths;
    }
    
    public HashMap<Vertex, HashMap<Vertex, T2<Double, Integer>>> getCosts() {
        return this.costs;
    }
    
    public void setCosts(HashMap<Vertex, HashMap<Vertex, T2<Double, Integer>>> costs) {
        this.costs = costs;
    }
    
}
