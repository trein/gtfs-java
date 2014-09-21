/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (props, at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.opentripplanner.standalone;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import org.opentripplanner.standalone.Graph.LoadLevel;

/**
 * This simple implementation of {@link GraphService} is mostly useful for testing
 *
 * @see GraphServiceImpl
 * @see GraphService
 */
public class GraphServiceBeanImpl implements GraphService {
    
    private Graph graph;
    
    // 0-arg bean constructor
    public GraphServiceBeanImpl() {
    }
    
    public GraphServiceBeanImpl(Graph graph, Preferences config) {
        this.graph = graph;
        GraphUpdaterConfigurator decorator = new GraphUpdaterConfigurator();
        decorator.setupGraph(graph, config);
    }
    
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    
    @Override
    public Graph getGraph() {
        return this.graph;
    }
    
    @Override
    public void setLoadLevel(LoadLevel level) {
        
    }
    
    @Override
    public Graph getGraph(String routerId) {
        return this.graph;
    }
    
    @Override
    public List<String> getRouterIds() {
        return Arrays.asList("default");
    }
    
    @Override
    public boolean registerGraph(String graphId, boolean preEvict) {
        return false;
    }
    
    @Override
    public boolean registerGraph(String graphId, Graph graph) {
        return false;
    }
    
    @Override
    public boolean evictGraph(String graphId) {
        return false;
    }
    
    @Override
    public int evictAll() {
        return 0;
    }
    
    @Override
    public boolean reloadGraphs(boolean preEvict) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean save(String routerId, InputStream is) {
        return false;
    }
    
}
