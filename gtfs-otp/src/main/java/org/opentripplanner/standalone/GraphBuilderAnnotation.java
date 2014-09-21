package org.opentripplanner.standalone;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents noteworthy events or errors that occur during the graphbuilding process. This is in
 * the routing subproject (rather than graphbuilder) to avoid making routing depend on the entire
 * graphbuilder subproject. Graphbuilder already depends on routing. Typically we want to create an
 * annotation object, store it in the graph that is being built, and log it at the same time.
 * Automatically logging in the annotation object constructor or in the Graph will lead to the wrong
 * compilation unit/line number being reported in the logs. It seems that we cannot modify the
 * behavior of the logger to report a log event one stack frame higher than usual because the true
 * logging mechanism is behind a facade. We cannot invert the process and log an annotation object
 * which would attach itself to a graph upon creation because the logger methods only accept
 * strings. Thus, a static register method on this class that creates an annotation, adds it to a
 * graph, and returns a message string for that annotation.
 *
 * @author andrewbyrd
 */
public abstract class GraphBuilderAnnotation implements Serializable {
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphBuilderAnnotation.class);
    
    private static final long serialVersionUID = 20121004L;
    
    // /** Generally, this should return the Vertex or Edge that is most relevant to this
    // annotation. */
    // public abstract Object getReferencedObject();

    @Override
    public String toString() {
        return "GraphBuilderAnnotation: " + this.getMessage();
    }
    
    public abstract String getMessage();
    
    public String getHTMLMessage() {
        return this.getMessage();
    }
    
    public Edge getReferencedEdge() {
        return null;
    }
    
    public Vertex getReferencedVertex() {
        return null;
    }
    
}
