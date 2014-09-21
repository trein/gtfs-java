package org.opentripplanner.standalone;

/**
 * Default implementation. Simply returns an instance of StreetVertexIndexServiceImpl.
 * 
 * @author avi
 */
public class DefaultStreetVertexIndexFactory implements StreetVertexIndexFactory {
    
    @Override
    public StreetVertexIndexService newIndex(Graph g) {
        return new StreetVertexIndexServiceImpl(g);
    }
}
