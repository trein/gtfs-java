package org.opentripplanner.standalone;

import java.io.Serializable;
import java.util.List;

public class MortonVertexComparatorFactory implements VertexComparatorFactory, Serializable {
    private static final long serialVersionUID = -6904862616793682390L;

    @Override
    public MortonVertexComparator getComparator(List<Vertex> domain) {
        return new MortonVertexComparator(domain);
    }

}
