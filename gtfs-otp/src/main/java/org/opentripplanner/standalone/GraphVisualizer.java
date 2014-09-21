package org.opentripplanner.standalone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javassist.Modifier;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Exit on window close.
 */
class ExitListener extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent event) {
        System.exit(0);
    }
}

/**
 * DisplayVertex holds a vertex, but has a toString value that's a little more useful.
 */
class DisplayVertex {
    public Vertex vertex;
    
    public DisplayVertex(Vertex v) {
        this.vertex = v;
    }
    
    @Override
    public String toString() {
        String label = this.vertex.getLabel();
        if (label.contains("osm node")) {
            label = this.vertex.getName();
        }
        return label;
    }
}

/**
 * This is a ListModel that holds Edges. It gets its edges from a PatternBoard/PatternAlight, hence
 * the iterable.
 */
class EdgeListModel extends AbstractListModel<Edge> {
    
    private static final long serialVersionUID = 1L;
    
    private final ArrayList<Edge> edges;
    
    EdgeListModel(Iterable<Edge> edges) {
        this.edges = new ArrayList<Edge>();
        for (Edge e : edges) {
            this.edges.add(e);
        }
    }
    
    @Override
    public int getSize() {
        return this.edges.size();
    }
    
    @Override
    public Edge getElementAt(int index) {
        return this.edges.get(index);
    }
}

/**
 * A list of vertices where the internal container is exposed.
 */
class VertexList extends AbstractListModel<DisplayVertex> {
    
    private static final long serialVersionUID = 1L;
    
    public List<Vertex> selected;
    
    VertexList(List<Vertex> selected) {
        this.selected = selected;
    }
    
    @Override
    public int getSize() {
        return this.selected.size();
    }
    
    @Override
    public DisplayVertex getElementAt(int index) {
        return new DisplayVertex(this.selected.get(index));
    }
};

/**
 * A simple visualizer for graphs. It shows (using ShowGraph) a map of the graph, intersections and
 * TransitStops only, and allows a user to select stops, examine incoming and outgoing edges, and
 * examine trip patterns. It's meant mainly for debugging, so it's totally OK if it develops (say) a
 * bunch of weird buttons designed to debug specific cases.
 */
public class GraphVisualizer extends JFrame implements VertexSelectionListener {
    
    private final class ComparePathStatesClickListener implements ListSelectionListener {
        private final JList<String> outputList;
        
        ComparePathStatesClickListener(JList<String> outputList) {
            this.outputList = outputList;
        }
        
        @Override
        public void valueChanged(ListSelectionEvent e) {
            @SuppressWarnings("unchecked")
            JList<State> theList = (JList<State>) e.getSource();
            State st = theList.getSelectedValue();
            if (st == null) { return; }
            
            DefaultListModel<String> stateListModel = new DefaultListModel<String>();
            stateListModel.addElement("weight:" + st.getWeight());
            stateListModel.addElement("weightdelta:" + st.getWeightDelta());
            stateListModel.addElement("bikeRenting:" + st.isBikeRenting());
            stateListModel.addElement("carParked:" + st.isCarParked());
            stateListModel.addElement("walkDistance:" + st.getWalkDistance());
            stateListModel.addElement("elapsedTime:" + st.getElapsedTimeSeconds());
            stateListModel.addElement("numBoardings:" + st.getNumBoardings());
            this.outputList.setModel(stateListModel);
            
            GraphVisualizer.this.lastStateClicked = st;
        }
    }
    
    private final class OnPopupMenuClickListener implements ActionListener {
        private final class DiffListCellRenderer extends DefaultListCellRenderer {
            private final int diverge;
            private final int converge;
            
            private DiffListCellRenderer(int diverge, int converge) {
                this.diverge = diverge;
                this.converge = converge;
            }
            
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) { return c; }
                
                if (index <= this.diverge) {
                    c.setBackground(new Color(196, 201, 255));
                }
                if (index >= this.converge) {
                    c.setBackground(new Color(255, 196, 196));
                }
                
                return c;
            }
        }
        
        private int[] diffPaths() {
            if ((GraphVisualizer.this.firstComparePath == null) || (GraphVisualizer.this.secondComparePath == null)) {
                int[] failboat = { -2, -2 };
                return failboat;
            }
            
            int l1 = GraphVisualizer.this.firstComparePath.states.size();
            int l2 = GraphVisualizer.this.secondComparePath.states.size();
            int minlen = l1 < l2 ? l1 : l2;
            
            int divergence = -1;
            int convergence = -1;
            
            // find divergence
            for (int i = 0; i < minlen; i++) {
                Vertex v1 = GraphVisualizer.this.firstComparePath.states.get(i).getVertex();
                Vertex v2 = GraphVisualizer.this.secondComparePath.states.get(i).getVertex();
                if (!v1.equals(v2)) {
                    divergence = i - 1;
                    break;
                }
            }
            
            // find convergence
            for (int i = 0; i < minlen; i++) {
                Vertex v1 = GraphVisualizer.this.firstComparePath.states.get(l1 - i - 1).getVertex();
                Vertex v2 = GraphVisualizer.this.secondComparePath.states.get(l2 - i - 1).getVertex();
                if (!v1.equals(v2)) {
                    convergence = i - 1;
                    break;
                }
            }
            
            int[] ret = { divergence, convergence };
            return ret;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            PathPrinter pp = (GraphVisualizer.this.pathsList.getSelectedValue());
            if (pp == null) { return; }
            GraphPath path = pp.gp;
            
            GraphVisualizer.this.firstComparePath = GraphVisualizer.this.secondComparePath;
            GraphVisualizer.this.secondComparePath = path;
            
            if (GraphVisualizer.this.firstComparePath != null) {
                DefaultListModel<State> pathModel = new DefaultListModel<State>();
                for (State st : GraphVisualizer.this.firstComparePath.states) {
                    pathModel.addElement(st);
                }
                GraphVisualizer.this.firstComparePathStates.setModel(pathModel);
            }
            if (GraphVisualizer.this.secondComparePath != null) {
                DefaultListModel<State> pathModel = new DefaultListModel<State>();
                for (State st : GraphVisualizer.this.secondComparePath.states) {
                    pathModel.addElement(st);
                }
                GraphVisualizer.this.secondComparePathStates.setModel(pathModel);
            }
            
            int[] diff = diffPaths();
            final int diverge = diff[0];
            final int converge = diff[1];
            if (diff[0] >= 0) {
                GraphVisualizer.this.firstComparePathStates.setCellRenderer(new DiffListCellRenderer(diverge,
                        GraphVisualizer.this.firstComparePath.states.size() - converge - 1));
                GraphVisualizer.this.secondComparePathStates.setCellRenderer(new DiffListCellRenderer(diverge,
                        GraphVisualizer.this.secondComparePath.states.size() - converge - 1));
            }
        }
    }
    
    class PathPrinter {
        GraphPath gp;
        
        PathPrinter(GraphPath gp) {
            this.gp = gp;
        }
        
        @Override
        public String toString() {
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("HH:mm:ss z");
            String startTime = shortDateFormat.format(new Date(this.gp.getStartTime() * 1000));
            String endTime = shortDateFormat.format(new Date(this.gp.getEndTime() * 1000));
            return "Path (" + startTime + "-" + endTime + ") weight:" + this.gp.getWeight() + " dur:"
                    + (this.gp.getDuration() / 60.0) + " walk:" + this.gp.getWalkDistance() + " nTrips:"
                    + this.gp.getTrips().size();
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LoggerFactory.getLogger(GraphVisualizer.class);
    
    private JPanel leftPanel;
    
    private ShowGraph showGraph;
    
    public JList<DisplayVertex> nearbyVertices;
    
    private JList<Edge> outgoingEdges;
    
    private JList<Edge> incomingEdges;
    
    private JTextField sourceVertex;
    
    private JTextField sinkVertex;
    
    private JCheckBox walkCheckBox;
    
    private JCheckBox bikeCheckBox;
    
    private JCheckBox trainCheckBox;
    
    private JCheckBox busCheckBox;
    
    private JCheckBox ferryCheckBox;
    
    private JCheckBox transitCheckBox;
    
    private JCheckBox carCheckBox;
    
    private JCheckBox cmvCheckBox;
    
    private JTextField searchDate;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    private JTextField boardingPenaltyField;
    
    private DefaultListModel<GraphBuilderAnnotation> annotationMatchesModel;
    
    private JList<GraphBuilderAnnotation> annotationMatches;

    private final ParetoPathService pathservice;
    
    private final GenericAStar sptService = new GenericAStar();
    
    private DefaultListModel<String> metadataModel;
    
    private HashSet<Vertex> closed;
    
    private Vertex tracingVertex;
    
    private HashSet<Vertex> open;
    
    private HashSet<Vertex> seen;
    
    private JList<String> metadataList;

    private final Graph graph;
    
    private JRadioButton opQuick;
    
    private JRadioButton opSafe;
    
    private JRadioButton opFlat;
    
    private JRadioButton opGreenways;
    
    private ButtonGroup optimizeTypeGrp;
    
    private JTextField maxWalkField;
    
    private JTextField walkSpeed;
    
    private JTextField bikeSpeed;
    
    private JTextField heuristicWeight;
    
    private JCheckBox softWalkLimiting;
    
    private JTextField softWalkPenalty;
    
    private JTextField softWalkOverageRate;
    
    private JCheckBox arriveByCheckBox;
    
    private JLabel searchTimeElapsedLabel;
    
    private JCheckBox dontUseGraphicalCallbackCheckBox;
    
    private JTextField nPaths;
    
    private JList<PathPrinter> pathsList;
    
    private JList<State> pathStates;
    
    private JCheckBox showTransitCheckbox;
    
    private JCheckBox showStreetsCheckbox;
    
    private JCheckBox showMultistateVerticesCheckbox;
    
    private JCheckBox showHighlightedCheckbox;
    
    private JCheckBox showSPTCheckbox;
    
    private ShortestPathTree spt;
    
    private JTextField sptFlattening;
    
    private JTextField sptThickness;
    
    private JPopupMenu popup;
    
    private GraphPath firstComparePath;
    private GraphPath secondComparePath;
    
    private JList<State> firstComparePathStates;
    private JList<State> secondComparePathStates;
    
    private JList<String> secondStateData;
    
    private JList<String> firstStateData;
    
    protected State lastStateClicked = null;
    
    public GraphVisualizer(GraphService graphService) {
        super();
        LOG.info("Starting up graph visualizer...");

        this.graph = graphService.getGraph();
        this.pathservice = new ParetoPathService(graphService, this.sptService);
        setTitle("GraphVisualizer");

        init();
    }
    
    public void run() {
        this.setVisible(true);
    }

    public void init() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        
        final Container mainTab = makeMainTab();
        Container prefsPanel = makePrefsPanel();
        Container diffTab = makeDiffTab();
        
        tabbedPane.addTab("Main", null, mainTab, "Pretty much everything");
        
        tabbedPane.addTab("Prefs", null, prefsPanel, "Routing preferences");
        
        tabbedPane.addTab("Diff", null, diffTab, "multistate path diffs");
        
        // Add the tabbed pane to this panel.
        add(tabbedPane);
        
        // The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // startup the graphical pane; ensure closing works; draw the window
        this.showGraph.init();
        addWindowListener(new ExitListener());
        pack();

        // make sure the showGraph quits drawing when we switch tabs
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedComponent().equals(mainTab)) {
                    GraphVisualizer.this.showGraph.loop();
                } else {
                    GraphVisualizer.this.showGraph.noLoop();
                }
            }
        });
    }
    
    private Container makeDiffTab() {
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(0, 2));

        this.firstStateData = new JList<String>();
        this.secondStateData = new JList<String>();

        // a place to list the states of the first path
        this.firstComparePathStates = new JList<State>();
        JScrollPane stScrollPane = new JScrollPane(this.firstComparePathStates);
        stScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.add(stScrollPane);
        this.firstComparePathStates.addListSelectionListener(new ComparePathStatesClickListener(this.firstStateData));

        // a place to list the states of the second path
        this.secondComparePathStates = new JList<State>();
        stScrollPane = new JScrollPane(this.secondComparePathStates);
        stScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.add(stScrollPane);
        this.secondComparePathStates.addListSelectionListener(new ComparePathStatesClickListener(this.secondStateData));

        // a place to list details of a state selected from the first path
        stScrollPane = new JScrollPane(this.firstStateData);
        stScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.add(stScrollPane);

        // a place to list details of a state selected from the second path
        stScrollPane = new JScrollPane(this.secondStateData);
        stScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.add(stScrollPane);

        // A button that executes the 'dominates' function between the two states
        // this is useful only if you have a breakpoint set up
        JButton dominateButton = new JButton();
        dominateButton.setText("dominates");
        dominateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State s1 = GraphVisualizer.this.firstComparePathStates.getSelectedValue();
                State s2 = GraphVisualizer.this.secondComparePathStates.getSelectedValue();
                
                System.out.println("s1 dominates s2:" + MultiShortestPathTree.dominates(s1, s2));
            }
        });
        pane.add(dominateButton);

        // A button that executes the 'traverse' function leading to the last clicked state
        // in either window. Also only useful if you set a breakpoint.
        JButton traverseButton = new JButton();
        traverseButton.setText("traverse");
        traverseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GraphVisualizer.this.lastStateClicked == null) { return; }
                
                Edge backEdge = GraphVisualizer.this.lastStateClicked.getBackEdge();
                State backState = GraphVisualizer.this.lastStateClicked.getBackState();
                
                backEdge.traverse(backState);
            }
        });
        pane.add(traverseButton);

        return pane;
    }
    
    private Container makeMainTab() {
        Container pane = new JPanel();
        pane.setLayout(new BorderLayout());
        
        // init center graphical panel
        this.showGraph = new ShowGraph(this, getGraph());
        pane.add(this.showGraph, BorderLayout.CENTER);
        this.sptService.setTraverseVisitor(new VisualTraverseVisitor(this.showGraph));
        
        // init left panel
        this.leftPanel = new JPanel();
        this.leftPanel.setLayout(new BorderLayout());
        
        pane.add(this.leftPanel, BorderLayout.LINE_START);
        
        initRoutingSubpanel();
        initVertexInfoSubpanel();
        initControlButtons();
        
        // init right panel
        initRightPanel(pane);
        return pane;
    }
    
    private JComponent makePrefsPanel() {
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(0, 2));

        // 2 rows: transport mode options
        this.walkCheckBox = new JCheckBox("walk");
        this.walkCheckBox.setSelected(true);
        pane.add(this.walkCheckBox);
        this.bikeCheckBox = new JCheckBox("bike");
        pane.add(this.bikeCheckBox);
        this.trainCheckBox = new JCheckBox("trainish");
        pane.add(this.trainCheckBox);
        this.busCheckBox = new JCheckBox("busish");
        pane.add(this.busCheckBox);
        this.ferryCheckBox = new JCheckBox("ferry");
        pane.add(this.ferryCheckBox);
        this.transitCheckBox = new JCheckBox("transit");
        this.transitCheckBox.setSelected(true);
        pane.add(this.transitCheckBox);
        this.carCheckBox = new JCheckBox("car");
        pane.add(this.carCheckBox);
        this.cmvCheckBox = new JCheckBox("custom vehicle");
        pane.add(this.cmvCheckBox);

        // row: arrive by?
        JLabel arriveByLabel = new JLabel("Arrive by?:");
        pane.add(arriveByLabel);
        this.arriveByCheckBox = new JCheckBox("arrive by");
        pane.add(this.arriveByCheckBox);
        
        // row: boarding penalty
        JLabel boardPenaltyLabel = new JLabel("Boarding penalty (min):");
        pane.add(boardPenaltyLabel);
        this.boardingPenaltyField = new JTextField("5");
        pane.add(this.boardingPenaltyField);

        // row: max walk
        JLabel maxWalkLabel = new JLabel("Maximum walk (meters):");
        pane.add(maxWalkLabel);
        this.maxWalkField = new JTextField("5000");
        pane.add(this.maxWalkField);

        // row: walk speed
        JLabel walkSpeedLabel = new JLabel("Walk speed (m/s):");
        pane.add(walkSpeedLabel);
        this.walkSpeed = new JTextField("1.33");
        pane.add(this.walkSpeed);

        // row: bike speed
        JLabel bikeSpeedLabel = new JLabel("Bike speed (m/s):");
        pane.add(bikeSpeedLabel);
        this.bikeSpeed = new JTextField("5.0");
        pane.add(this.bikeSpeed);

        // row: heuristic weight
        JLabel heuristicWeightLabel = new JLabel("Heuristic weight:");
        pane.add(heuristicWeightLabel);
        this.heuristicWeight = new JTextField("1.0");
        pane.add(this.heuristicWeight);

        // row: soft walk?
        JLabel softWalkLimitLabel = new JLabel("Soft walk-limit?:");
        pane.add(softWalkLimitLabel);
        this.softWalkLimiting = new JCheckBox("soft walk-limiting");
        pane.add(this.softWalkLimiting);

        // row: soft walk-limit penalty
        JLabel softWalkLimitPenaltyLabel = new JLabel("Soft walk-limiting penalty:");
        pane.add(softWalkLimitPenaltyLabel);
        this.softWalkPenalty = new JTextField("60.0");
        pane.add(this.softWalkPenalty);

        // row: soft walk-limit overage
        JLabel softWalkLimitOverageLabel = new JLabel("Soft walk-limiting overage:");
        pane.add(softWalkLimitOverageLabel);
        this.softWalkOverageRate = new JTextField("5.0");
        pane.add(this.softWalkOverageRate);

        // row: nPaths
        JLabel nPathsLabel = new JLabel("nPaths:");
        pane.add(nPathsLabel);
        this.nPaths = new JTextField("1");
        pane.add(this.nPaths);

        // viz preferences
        ItemListener onChangeVizPrefs = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                GraphVisualizer.this.showGraph.setShowTransit(GraphVisualizer.this.showTransitCheckbox.isSelected());
                GraphVisualizer.this.showGraph.setShowStreets(GraphVisualizer.this.showStreetsCheckbox.isSelected());
                GraphVisualizer.this.showGraph.setShowMultistateVertices(GraphVisualizer.this.showMultistateVerticesCheckbox
                        .isSelected());
                GraphVisualizer.this.showGraph.setShowHightlights(GraphVisualizer.this.showHighlightedCheckbox.isSelected());
                GraphVisualizer.this.showGraph.setShowSPT(GraphVisualizer.this.showSPTCheckbox.isSelected());
                GraphVisualizer.this.showGraph.redraw();
            }
        };
        this.showTransitCheckbox = new JCheckBox("show transit");
        this.showTransitCheckbox.setSelected(true);
        this.showTransitCheckbox.addItemListener(onChangeVizPrefs);
        pane.add(this.showTransitCheckbox);
        this.showStreetsCheckbox = new JCheckBox("show streets");
        this.showStreetsCheckbox.setSelected(true);
        this.showStreetsCheckbox.addItemListener(onChangeVizPrefs);
        pane.add(this.showStreetsCheckbox);
        this.showHighlightedCheckbox = new JCheckBox("show highlighted");
        this.showHighlightedCheckbox.setSelected(true);
        this.showHighlightedCheckbox.addItemListener(onChangeVizPrefs);
        pane.add(this.showHighlightedCheckbox);
        this.showSPTCheckbox = new JCheckBox("show SPT");
        this.showSPTCheckbox.setSelected(true);
        this.showSPTCheckbox.addItemListener(onChangeVizPrefs);
        pane.add(this.showSPTCheckbox);
        this.showMultistateVerticesCheckbox = new JCheckBox("show multistate vertices");
        this.showMultistateVerticesCheckbox.setSelected(true);
        this.showMultistateVerticesCheckbox.addItemListener(onChangeVizPrefs);
        pane.add(this.showMultistateVerticesCheckbox);
        
        // row: SPT flattening
        JLabel sptFlatteningLabel = new JLabel("SPT flattening:");
        pane.add(sptFlatteningLabel);
        this.sptFlattening = new JTextField("0.3");
        pane.add(this.sptFlattening);
        
        // row: SPT thickness
        JLabel sptThicknessLabel = new JLabel("SPT thickness:");
        pane.add(sptThicknessLabel);
        this.sptThickness = new JTextField("0.1");
        pane.add(this.sptThickness);

        // radio buttons: optimize type
        JLabel optimizeTypeLabel = new JLabel("Optimize type:");
        pane.add(optimizeTypeLabel);
        
        this.opQuick = new JRadioButton("Quick");
        this.opQuick.setSelected(true);
        this.opSafe = new JRadioButton("Safe");
        this.opFlat = new JRadioButton("Flat");
        this.opGreenways = new JRadioButton("Greenways");

        this.optimizeTypeGrp = new ButtonGroup();
        this.optimizeTypeGrp.add(this.opQuick);
        this.optimizeTypeGrp.add(this.opSafe);
        this.optimizeTypeGrp.add(this.opFlat);
        this.optimizeTypeGrp.add(this.opGreenways);

        JPanel optimizeTypePane = new JPanel();
        optimizeTypePane.add(this.opQuick);
        optimizeTypePane.add(this.opSafe);
        optimizeTypePane.add(this.opFlat);
        optimizeTypePane.add(this.opGreenways);

        pane.add(optimizeTypePane);
        
        return pane;
    }
    
    OptimizeType getSelectedOptimizeType() {
        if (this.opQuick.isSelected()) { return OptimizeType.QUICK; }
        if (this.opSafe.isSelected()) { return OptimizeType.SAFE; }
        if (this.opFlat.isSelected()) { return OptimizeType.FLAT; }
        if (this.opGreenways.isSelected()) { return OptimizeType.GREENWAYS; }
        return OptimizeType.QUICK;
    }
    
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(SwingConstants.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    private void initRightPanel(Container pane) {
        /* right panel holds trip pattern and stop metadata */
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        pane.add(rightPanel, BorderLayout.LINE_END);
        
        JTabbedPane rightPanelTabs = new JTabbedPane();
        
        rightPanel.add(rightPanelTabs, BorderLayout.LINE_END);

        // a place to print out the details of a path
        this.pathStates = new JList<State>();
        JScrollPane stScrollPane = new JScrollPane(this.pathStates);
        stScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        rightPanelTabs.addTab("path states", stScrollPane);

        // when you select a path component state, it prints the backedge's metadata
        this.pathStates.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                GraphVisualizer.this.outgoingEdges.clearSelection();
                GraphVisualizer.this.incomingEdges.clearSelection();
                
                @SuppressWarnings("unchecked")
                JList<State> theList = (JList<State>) e.getSource();
                State st = theList.getSelectedValue();
                Edge edge = st.getBackEdge();
                reactToEdgeSelection(edge, false);
            }
        });
        
        this.metadataList = new JList<String>();
        this.metadataModel = new DefaultListModel<String>();
        this.metadataList.setModel(this.metadataModel);
        JScrollPane mdScrollPane = new JScrollPane(this.metadataList);
        mdScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        rightPanelTabs.addTab("metadata", mdScrollPane);
        
        // This is where matched annotations from an annotation search go
        this.annotationMatches = new JList<GraphBuilderAnnotation>();
        this.annotationMatches.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                @SuppressWarnings("unchecked")
                JList<GraphBuilderAnnotation> theList = (JList<GraphBuilderAnnotation>) e.getSource();

                GraphBuilderAnnotation anno = theList.getSelectedValue();
                if (anno == null) { return; }
                GraphVisualizer.this.showGraph.drawAnotation(anno);
            }
        });
        
        this.annotationMatchesModel = new DefaultListModel<GraphBuilderAnnotation>();
        this.annotationMatches.setModel(this.annotationMatchesModel);
        JScrollPane amScrollPane = new JScrollPane(this.annotationMatches);
        amScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        rightPanelTabs.addTab("annotations", amScrollPane);
        
        Dimension size = new Dimension(200, 1600);
        
        amScrollPane.setMaximumSize(size);
        amScrollPane.setPreferredSize(size);
        stScrollPane.setMaximumSize(size);
        stScrollPane.setPreferredSize(size);
        mdScrollPane.setMaximumSize(size);
        mdScrollPane.setPreferredSize(size);
        rightPanelTabs.setMaximumSize(size);
        rightPanel.setMaximumSize(size);
    }
    
    private void initControlButtons() {
        /* buttons at bottom */
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 3));
        this.leftPanel.add(buttonPanel, BorderLayout.PAGE_END);
        
        JButton zoomDefaultButton = new JButton("Zoom to default");
        zoomDefaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphVisualizer.this.showGraph.zoomToDefault();
            }
        });
        buttonPanel.add(zoomDefaultButton);
        
        final JFrame frame = this;
        
        JButton zoomToNodeButton = new JButton("Zoom to node");
        zoomToNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeName = JOptionPane.showInputDialog(frame, "Node id", JOptionPane.PLAIN_MESSAGE);
                Vertex v = getGraph().getVertex(nodeName);
                if (v == null) {
                    System.out.println("no such node " + nodeName);
                } else {
                    GraphVisualizer.this.showGraph.zoomToVertex(v);
                }
            }
        });
        buttonPanel.add(zoomToNodeButton);
        
        JButton zoomToLocationButton = new JButton("Zoom to location");
        zoomToLocationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog("Enter the location (lat lon)");
                if ((result == null) || (result.length() == 0)) { return; }
                String[] tokens = result.split("[\\s,]+");
                double lat = Double.parseDouble(tokens[0]);
                double lon = Double.parseDouble(tokens[1]);
                Coordinate c = new Coordinate(lon, lat);
                GraphVisualizer.this.showGraph.zoomToLocation(c);
            }
        });
        buttonPanel.add(zoomToLocationButton);
        
        JButton zoomOutButton = new JButton("Zoom out");
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphVisualizer.this.showGraph.zoomOut();
            }
        });
        buttonPanel.add(zoomOutButton);
        
        JButton routeButton2 = new JButton("Route");
        routeButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // String initialFrom = "";
                // Object selected = nearbyVertices.getSelectedValue();
                // if (selected != null) {
                // initialFrom = selected.toString();
                // }
                // RouteDialog dlg = new RouteDialog(frame, initialFrom); // modal
                String from = GraphVisualizer.this.sourceVertex.getText();
                String to = GraphVisualizer.this.sinkVertex.getText();
                route(from, to);
            }
        });
        buttonPanel.add(routeButton2);
        
        JButton findButton = new JButton("Find node");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeName = JOptionPane.showInputDialog(frame, "Node id", JOptionPane.PLAIN_MESSAGE);
                Vertex v = getGraph().getVertex(nodeName);
                if (v == null) {
                    System.out.println("no such node " + nodeName);
                } else {
                    GraphVisualizer.this.showGraph.highlightVertex(v);
                    ArrayList<Vertex> l = new ArrayList<Vertex>();
                    l.add(v);
                    verticesSelected(l);
                }
            }
        });
        buttonPanel.add(findButton);
        
        JButton findEdgeButton = new JButton("Find edge");
        findEdgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String edgeName = JOptionPane.showInputDialog(frame, "Edge name like", JOptionPane.PLAIN_MESSAGE);
                for (Vertex gv : getGraph().getVertices()) {
                    for (Edge edge : gv.getOutgoing()) {
                        if ((edge.getName() != null) && edge.getName().contains(edgeName)) {
                            GraphVisualizer.this.showGraph.highlightVertex(gv);
                            ArrayList<Vertex> l = new ArrayList<Vertex>();
                            l.add(gv);
                            verticesSelected(l);
                        }
                    }
                }
            }
        });
        buttonPanel.add(findEdgeButton);
        
        JButton checkButton = new JButton("Check graph");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGraph();
            }
        });
        buttonPanel.add(checkButton);
        
        JButton traceButton = new JButton("Trace");
        traceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trace();
            }
        });
        buttonPanel.add(traceButton);
        
        // annotation search button
        JButton annotationButton = new JButton("Find annotations");
        annotationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findAnnotation();
            }
        });
        buttonPanel.add(annotationButton);
        
        JButton findEdgeByIdButton = new JButton("Find edge ID");
        findEdgeByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String edgeIdStr = JOptionPane.showInputDialog(frame, "Edge ID", JOptionPane.PLAIN_MESSAGE);
                Integer edgeId = Integer.parseInt(edgeIdStr);
                Edge edge = getGraph().getEdgeById(edgeId);
                if (edge != null) {
                    GraphVisualizer.this.showGraph.highlightEdge(edge);
                    GraphVisualizer.this.showGraph.highlightVertex(edge.getFromVertex());
                } else {
                    System.out.println("Found no edge with ID " + edgeIdStr);
                }
            }
        });
        buttonPanel.add(findEdgeByIdButton);

        JButton snapButton = new JButton("Snap location");
        snapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String locString = JOptionPane.showInputDialog(frame, "Location string", "");
                GenericLocation loc = GenericLocation.fromOldStyleString(locString);
                RoutingRequest rr = new RoutingRequest();
                Vertex v = GraphVisualizer.this.graph.streetIndex.getVertexForLocation(loc, rr);
                GraphVisualizer.this.showGraph.highlightVertex(v);
            }
        });
        buttonPanel.add(snapButton);
    }
    
    private void getMetadata(Object selected) {
        Class<?> c = selected.getClass();
        Field[] fields;
        while ((c != null) && (c != Object.class)) {
            this.metadataModel.addElement("Class:" + c);
            fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int modifiers = field.getModifiers();
                if ((modifiers & Modifier.STATIC) != 0) {
                    continue;
                }
                field.setAccessible(true);
                String name = field.getName();
                
                String value = "(unknown -- see console for stack trace)";
                try {
                    value = "" + field.get(selected);
                } catch (IllegalArgumentException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                this.metadataModel.addElement(name + ": " + value);
            }
            c = c.getSuperclass();
        }
    }
    
    private void reactToEdgeSelection(Edge selected, boolean outgoing) {
        if (selected == null) { return; }
        this.showGraph.highlightEdge(selected);
        
        /* for turns, highlight the outgoing street's ends */
        if (selected instanceof StreetEdge) {
            List<Vertex> vertices = new ArrayList<Vertex>();
            List<Edge> edges = new ArrayList<Edge>();
            Vertex tov = selected.getToVertex();
            for (Edge og : tov.getOutgoing()) {
                if (og instanceof StreetEdge) {
                    edges.add(og);
                    vertices.add(og.getToVertex());
                    break;
                }
            }
            Vertex fromv = selected.getFromVertex();
            for (Edge ic : fromv.getIncoming()) {
                if (ic instanceof StreetEdge) {
                    edges.add(ic);
                    vertices.add(ic.getFromVertex());
                    break;
                }
            }
            // showGraph.setHighlightedVertices(vertices);
            this.showGraph.setHighlightedEdges(edges);
        }
        
        /* add the connected vertices to the list of vertices */
        VertexList nearbyModel = (VertexList) this.nearbyVertices.getModel();
        List<Vertex> vertices = nearbyModel.selected;
        
        Vertex v;
        if (outgoing) {
            v = selected.getToVertex();
        } else {
            v = selected.getFromVertex();
        }
        if (!vertices.contains(v)) {
            vertices.add(v);
            nearbyModel = new VertexList(vertices);
            this.nearbyVertices.setModel(nearbyModel); // this should just be an event, but for
            // some reason, JList doesn't implement
            // the right event.
        }
        
        /* set up metadata tab */
        this.metadataModel.clear();
        getMetadata(selected);
        // fromv
        Vertex fromv = selected.getFromVertex();
        getMetadata(fromv);
        if (selected instanceof EdgeWithElevation) {
            getMetadata(((EdgeWithElevation) selected).getElevationProfileSegment());
        }
        this.metadataList.revalidate();
        
    }
    
    private void initVertexInfoSubpanel() {
        JPanel vertexDataPanel = new JPanel();
        vertexDataPanel.setLayout(new BoxLayout(vertexDataPanel, BoxLayout.PAGE_AXIS));
        vertexDataPanel.setPreferredSize(new Dimension(300, 600));
        this.leftPanel.add(vertexDataPanel, BorderLayout.CENTER);
        
        // nearby vertices
        JLabel nvLabel = new JLabel("Vertices");
        vertexDataPanel.add(nvLabel);
        this.nearbyVertices = new JList<DisplayVertex>();
        this.nearbyVertices.setVisibleRowCount(4);
        JScrollPane nvScrollPane = new JScrollPane(this.nearbyVertices);
        vertexDataPanel.add(nvScrollPane);
        this.nearbyVertices.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                GraphVisualizer.this.outgoingEdges.removeAll();
                GraphVisualizer.this.incomingEdges.removeAll();
                DisplayVertex selected = GraphVisualizer.this.nearbyVertices.getSelectedValue();
                if (selected != null) {
                    Vertex nowSelected = selected.vertex;
                    GraphVisualizer.this.showGraph.highlightVertex(nowSelected);
                    GraphVisualizer.this.outgoingEdges.setModel(new EdgeListModel(nowSelected.getOutgoing()));
                    GraphVisualizer.this.incomingEdges.setModel(new EdgeListModel(nowSelected.getIncoming()));
                }
            }
        });

        // listener useful for both incoming and outgoing edge list panes
        // when a different edge is selected, change up the pattern pane and list of nearby nodes
        ListSelectionListener edgeChanged = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                @SuppressWarnings("unchecked")
                JList<Edge> edgeList = (JList<Edge>) e.getSource();

                Edge selected = edgeList.getSelectedValue();

                boolean outgoing = (edgeList == GraphVisualizer.this.outgoingEdges);
                reactToEdgeSelection(selected, outgoing);
            }
            
        };
        
        // outgoing edges
        JLabel ogeLabel = new JLabel("Outgoing edges");
        vertexDataPanel.add(ogeLabel);
        this.outgoingEdges = new JList<Edge>();
        this.outgoingEdges.setVisibleRowCount(4);
        JScrollPane ogeScrollPane = new JScrollPane(this.outgoingEdges);
        vertexDataPanel.add(ogeScrollPane);
        this.outgoingEdges.addListSelectionListener(edgeChanged);
        
        // incoming edges
        JLabel iceLabel = new JLabel("Incoming edges");
        vertexDataPanel.add(iceLabel);
        this.incomingEdges = new JList<Edge>();
        JScrollPane iceScrollPane = new JScrollPane(this.incomingEdges);
        vertexDataPanel.add(iceScrollPane);
        this.incomingEdges.addListSelectionListener(edgeChanged);
        
        // paths list
        JLabel pathsLabel = new JLabel("Paths");
        vertexDataPanel.add(pathsLabel);
        this.pathsList = new JList<PathPrinter>();

        this.popup = new JPopupMenu();
        JMenuItem compareMenuItem = new JMenuItem("compare");
        compareMenuItem.addActionListener(new OnPopupMenuClickListener());
        this.popup.add(compareMenuItem);

        // make paths list right-clickable
        this.pathsList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    @SuppressWarnings("unchecked")
                    JList<PathPrinter> list = (JList<PathPrinter>) e.getSource();
                    int row = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(row);
                    
                    GraphVisualizer.this.popup.show(list, e.getX(), e.getY());
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        this.pathsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent ev) {
                
                PathPrinter pp = (GraphVisualizer.this.pathsList.getSelectedValue());
                if (pp == null) { return; }
                GraphPath path = pp.gp;
                
                DefaultListModel<State> pathModel = new DefaultListModel<State>();
                for (State st : path.states) {
                    pathModel.addElement(st);
                }
                GraphVisualizer.this.pathStates.setModel(pathModel);
                
                GraphVisualizer.this.showGraph.highlightGraphPath(path);
            }
            
        });
        JScrollPane pathsScrollPane = new JScrollPane(this.pathsList);
        vertexDataPanel.add(pathsScrollPane);
    }
    
    private void initRoutingSubpanel() {
        /* ROUTING SUBPANEL */
        JPanel routingPanel = new JPanel();
        routingPanel.setLayout(new GridLayout(0, 2));
        this.leftPanel.add(routingPanel, BorderLayout.NORTH);
        
        // row: source vertex
        JButton setSourceVertexButton = new JButton("set source");
        setSourceVertexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = GraphVisualizer.this.nearbyVertices.getSelectedValue();
                if (selected != null) {
                    GraphVisualizer.this.sourceVertex.setText(selected.toString());
                }
            }
        });
        routingPanel.add(setSourceVertexButton);
        this.sourceVertex = new JTextField();
        routingPanel.add(this.sourceVertex);
        
        // row: sink vertex
        JButton setSinkVertexButton = new JButton("set sink");
        setSinkVertexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = GraphVisualizer.this.nearbyVertices.getSelectedValue();
                if (selected != null) {
                    GraphVisualizer.this.sinkVertex.setText(selected.toString());
                }
            }
        });
        routingPanel.add(setSinkVertexButton);
        this.sinkVertex = new JTextField();
        routingPanel.add(this.sinkVertex);
        
        // row: set date
        JButton resetSearchDateButton = new JButton("now ->");
        resetSearchDateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphVisualizer.this.searchDate.setText(GraphVisualizer.this.dateFormat.format(new Date()));
            }
        });
        routingPanel.add(resetSearchDateButton);
        this.searchDate = new JTextField();
        this.searchDate.setText(this.dateFormat.format(new Date()));
        routingPanel.add(this.searchDate);
        
        // row: launch, continue, and clear path search
        JButton routeButton = new JButton("path search");
        routeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String from = GraphVisualizer.this.sourceVertex.getText();
                String to = GraphVisualizer.this.sinkVertex.getText();
                route(from, to);
            }
        });
        routingPanel.add(routeButton);
        JButton continueButton = new JButton("continue");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO continue search
            }
        });
        routingPanel.add(continueButton);
        JButton clearRouteButton = new JButton("clear path");
        clearRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphVisualizer.this.showGraph.highlightGraphPath(null);
                GraphVisualizer.this.showGraph.clearHighlights();
                GraphVisualizer.this.showGraph.resetSPT();
            }
        });
        routingPanel.add(clearRouteButton);

        // label: search time elapsed
        this.searchTimeElapsedLabel = new JLabel("search time elapsed:");
        routingPanel.add(this.searchTimeElapsedLabel);

        // option: don't use graphical callback. useful for doing a quick profile
        this.dontUseGraphicalCallbackCheckBox = new JCheckBox("no graphics");
        routingPanel.add(this.dontUseGraphicalCallbackCheckBox);
    }
    
    protected void trace() {
        DisplayVertex selected = this.nearbyVertices.getSelectedValue();
        if (selected == null) { return; }
        Vertex v = selected.vertex;
        
        if (this.tracingVertex != v) {
            this.tracingVertex = v;
            this.closed = new HashSet<Vertex>();
            this.open = new HashSet<Vertex>();
            this.open.add(v);
            this.seen = new HashSet<Vertex>();
        }
        HashSet<Vertex> newOpen = new HashSet<Vertex>();
        for (Vertex v2 : this.open) {
            this.closed.add(v2);
            for (Edge e : v2.getOutgoing()) {
                Vertex target = e.getToVertex();
                if (this.closed.contains(target)) {
                    continue;
                }
                newOpen.add(target);
            }
        }
        this.seen.addAll(newOpen);
        this.open = newOpen;
        this.showGraph.setHighlightedVertices(this.seen);
    }
    
    protected void traceOld() {
        HashSet<Vertex> seenVertices = new HashSet<Vertex>();
        DisplayVertex selected = this.nearbyVertices.getSelectedValue();
        if (selected == null) {
            System.out.println("no vertex selected");
            return;
        }
        Vertex v = selected.vertex;
        System.out.println("initial vertex: " + v);
        Queue<Vertex> toExplore = new LinkedList<Vertex>();
        toExplore.add(v);
        seenVertices.add(v);
        while (!toExplore.isEmpty()) {
            Vertex src = toExplore.poll();
            for (Edge e : src.getOutgoing()) {
                Vertex tov = e.getToVertex();
                if (!seenVertices.contains(tov)) {
                    seenVertices.add(tov);
                    toExplore.add(tov);
                }
            }
        }
        this.showGraph.setHighlightedVertices(seenVertices);
    }
    
    protected void checkGraph() {
        
        HashSet<Vertex> seenVertices = new HashSet<Vertex>();
        Collection<Vertex> allVertices = getGraph().getVertices();
        Vertex v = allVertices.iterator().next();
        System.out.println("initial vertex: " + v);
        Queue<Vertex> toExplore = new LinkedList<Vertex>();
        toExplore.add(v);
        seenVertices.add(v);
        while (!toExplore.isEmpty()) {
            Vertex src = toExplore.poll();
            for (Edge e : src.getOutgoing()) {
                Vertex tov = e.getToVertex();
                if (!seenVertices.contains(tov)) {
                    seenVertices.add(tov);
                    toExplore.add(tov);
                }
            }
        }
        
        System.out.println("After investigation, visited " + seenVertices.size() + " of " + allVertices.size());
        
        /* now, let's find an unvisited vertex */
        for (Vertex u : allVertices) {
            if (!seenVertices.contains(u)) {
                System.out.println("unvisited vertex" + u);
                break;
            }
        }
    }
    
    protected void route(String from, String to) {
        Date when;
        // Year + 1900
        try {
            when = this.dateFormat.parse(this.searchDate.getText());
        } catch (ParseException e) {
            this.searchDate.setText("Format: " + this.dateFormat.toPattern());
            return;
        }
        TraverseModeSet modeSet = new TraverseModeSet();
        modeSet.setWalk(this.walkCheckBox.isSelected());
        modeSet.setBicycle(this.bikeCheckBox.isSelected());
        modeSet.setFerry(this.ferryCheckBox.isSelected());
        modeSet.setTrainish(this.trainCheckBox.isSelected());
        modeSet.setBusish(this.busCheckBox.isSelected());
        modeSet.setCar(this.carCheckBox.isSelected());
        modeSet.setCustomMotorVehicle(this.cmvCheckBox.isSelected());
        // must set generic transit mode last, and only when it is checked
        // otherwise 'false' will clear trainish and busish
        if (this.transitCheckBox.isSelected()) {
            modeSet.setTransit(true);
        }
        RoutingRequest options = new RoutingRequest(modeSet);
        options.setArriveBy(this.arriveByCheckBox.isSelected());
        options.setWalkBoardCost(Integer.parseInt(this.boardingPenaltyField.getText()) * 60); // override
                                                                                              // low
                                                                                              // 2-4
                                                                                              // minute
                                                                                              // values
        // TODO LG Add ui element for bike board cost (for now bike = 2 * walk)
        options.setBikeBoardCost(Integer.parseInt(this.boardingPenaltyField.getText()) * 60 * 2);
        // there should be a ui element for walk distance and optimize type
        options.setOptimize(getSelectedOptimizeType());
        options.setMaxWalkDistance(Integer.parseInt(this.maxWalkField.getText()));
        options.setDateTime(when);
        options.setFromString(from);
        options.setToString(to);
        options.walkSpeed = Float.parseFloat(this.walkSpeed.getText());
        options.bikeSpeed = Float.parseFloat(this.bikeSpeed.getText());
        options.heuristicWeight = (Float.parseFloat(this.heuristicWeight.getText()));
        options.softWalkLimiting = (this.softWalkLimiting.isSelected());
        options.softWalkPenalty = (Float.parseFloat(this.softWalkPenalty.getText()));
        options.softWalkOverageRate = (Float.parseFloat(this.softWalkOverageRate.getText()));
        options.numItineraries = 1;
        System.out.println("--------");
        System.out.println("Path from " + from + " to " + to + " at " + when);
        System.out.println("\tModes: " + modeSet);
        System.out.println("\tOptions: " + options);

        options.numItineraries = (Integer.parseInt(this.nPaths.getText()));

        // apply callback if the options call for it
        if (this.dontUseGraphicalCallbackCheckBox.isSelected()) {
            this.sptService.setTraverseVisitor(null);
        } else {
            this.sptService.setTraverseVisitor(new VisualTraverseVisitor(this.showGraph));
        }

        // set up a visitor to the path service so we can get the SPT as it's generated
        ParetoPathService.SPTVisitor vis = this.pathservice.new SPTVisitor();
        this.pathservice.setSPTVisitor(vis);

        long t0 = System.currentTimeMillis();
        // TODO: check options properly intialized (AMB)
        List<GraphPath> paths = this.pathservice.getPaths(options);
        long dt = System.currentTimeMillis() - t0;
        this.searchTimeElapsedLabel.setText("search time elapsed: " + dt + "ms");

        // grab the spt from the visitor
        this.spt = vis.spt;
        this.showGraph.setSPT(this.spt);
        System.out.println("got spt:" + this.spt);

        if (paths == null) {
            System.out.println("no path");
            this.showGraph.highlightGraphPath(null);
            return;
        }

        // now's a convenient time to set graphical SPT weights
        this.showGraph.simpleSPT.setWeights();
        
        showPathsInPanel(paths);

        // now's a good time to set showGraph's SPT drawing weights
        this.showGraph.setSPTFlattening(Float.parseFloat(this.sptFlattening.getText()));
        this.showGraph.setSPTThickness(Float.parseFloat(this.sptThickness.getText()));
        this.showGraph.redraw();

        options.cleanup();
    }
    
    private void showPathsInPanel(List<GraphPath> paths) {
        // show paths in a list panel
        DefaultListModel<PathPrinter> data = new DefaultListModel<PathPrinter>();
        for (GraphPath gp : paths) {
            data.addElement(new PathPrinter(gp));
        }
        this.pathsList.setModel(data);
    }
    
    protected void findAnnotation() {
        Set<Class<? extends GraphBuilderAnnotation>> gbaClasses = Sets.newHashSet();
        for (GraphBuilderAnnotation gba : this.graph.getBuilderAnnotations()) {
            gbaClasses.add(gba.getClass());
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends GraphBuilderAnnotation> variety = (Class<? extends GraphBuilderAnnotation>) JOptionPane.showInputDialog(
                null, // parentComponent; TODO: set correctly
                "Select the type of annotation to find", // question
                "Select annotation", // title
                JOptionPane.QUESTION_MESSAGE, // message type
                null, // no icon
                gbaClasses.toArray(), // options (built above)
                StopUnlinked.class // default value
                );
        
        // User clicked cancel
        if (variety == null) { return; }
        
        // loop over the annotations and save the ones of the requested type
        this.annotationMatchesModel.clear();
        for (GraphBuilderAnnotation anno : this.graph.getBuilderAnnotations()) {
            if (variety.isInstance(anno)) {
                this.annotationMatchesModel.addElement(anno);
            }
        }
        
        System.out.println("Found " + this.annotationMatchesModel.getSize() + " annotations of type " + variety);
        
    }
    
    public void verticesSelected(final List<Vertex> selected) {
        // sort vertices by name
        Collections.sort(selected, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex arg0, Vertex arg1) {
                return arg0.getLabel().compareTo(arg1.getLabel());
            }
            
        });
        ListModel<DisplayVertex> data = new VertexList(selected);
        this.nearbyVertices.setModel(data);
        
        // pick out an intersection vertex and find the path
        // if the spt is already available
        Vertex target = null;
        for (Vertex vv : selected) {
            if (vv instanceof IntersectionVertex) {
                target = vv;
                break;
            }
        }
        if ((target != null) && (this.spt != null)) {
            List<GraphPath> paths = this.spt.getPaths(target, true);
            showPathsInPanel(paths);
        }
    }
    
    public Graph getGraph() {
        return this.graph;
    }
    
}
