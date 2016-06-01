/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing.ch;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighborListGraphEntityFactory;
import cz.certicon.routing.model.entity.neighbourlist.NeighborListGraphEntityFactory;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OptimizedContractionHierarchiesPreprocessorTest {

    private final Graph graph;
    private final NeighborListGraphEntityFactory graphEntityFactory;
    private final LengthDistanceFactory distanceFactory;

    public OptimizedContractionHierarchiesPreprocessorTest() {
        graphEntityFactory = new NeighborListGraphEntityFactory();
        distanceFactory = new LengthDistanceFactory();
        graph = graphEntityFactory.createGraph();
        Node a = graphEntityFactory.createNode( Node.Id.createId( 1 ), 50.1001831, 14.3856114 );
        Node b = graphEntityFactory.createNode( Node.Id.createId( 2 ), 50.1002725, 14.3872906 );
        Node c = graphEntityFactory.createNode( Node.Id.createId( 3 ), 50.1018347, 14.3857995 );
        Node d = graphEntityFactory.createNode( Node.Id.createId( 4 ), 50.1017039, 14.3871028 );
        Node e = graphEntityFactory.createNode( Node.Id.createId( 5 ), 50.1002828, 14.3878056 );
        Node f = graphEntityFactory.createNode( Node.Id.createId( 6 ), 50.1016489, 14.3876339 );
        a.setLabel( "a" );
        b.setLabel( "b" );
        c.setLabel( "c" );
        d.setLabel( "d" );
        e.setLabel( "e" );
        f.setLabel( "f" );
        Edge ab = createEdge( graphEntityFactory, distanceFactory, a, b );
        Edge ba = createEdge( graphEntityFactory, distanceFactory, b, a );
        Edge ac = createEdge( graphEntityFactory, distanceFactory, a, c );
        Edge ca = createEdge( graphEntityFactory, distanceFactory, c, a );
        Edge db = createEdge( graphEntityFactory, distanceFactory, d, b );
        Edge cd = createEdge( graphEntityFactory, distanceFactory, c, d );
        Edge dc = createEdge( graphEntityFactory, distanceFactory, d, c );
        Edge be = createEdge( graphEntityFactory, distanceFactory, b, e );
        Edge eb = createEdge( graphEntityFactory, distanceFactory, e, b );
        Edge df = createEdge( graphEntityFactory, distanceFactory, d, f );
        Edge fd = createEdge( graphEntityFactory, distanceFactory, f, d );
        Edge ef = createEdge( graphEntityFactory, distanceFactory, e, f );
        graph.addNode( a ).addNode( b ).addNode( c ).addNode( d ).addNode( e ).addNode( f );
        graph.addEdge( ab )
                .addEdge( ba )
                .addEdge( ac )
                .addEdge( ca )
                .addEdge( db )
                .addEdge( cd )
                .addEdge( dc )
                .addEdge( be )
                .addEdge( eb )
                .addEdge( df )
                .addEdge( fd )
                .addEdge( ef );
    }

    private static Edge createEdge( GraphEntityFactory entityFactory, DistanceFactory distanceFactory, Node sourceNode, Node targetNode ) {
        EdgeData edgeData = new SimpleEdgeData( 50, false, CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) );
        EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder().setLength( edgeData.getLength() ).build();
        Edge e = entityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode, distanceFactory.createFromEdgeData( edgeData ) );
        e.setAttributes( edgeAttributes );
        return e;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of preprocess method, of class
     * OptimizedContractionHierarchiesPreprocessor.
     */
    @Test
    public void testPreprocess_3args() {
        System.out.println( "preprocess" );
//        GlobalOptions.DEBUG_CORRECTNESS = false;
//        OptimizedContractionHierarchiesPreprocessor instance = new OptimizedContractionHierarchiesPreprocessor();
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> expResult = null;
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> result = instance.preprocess( graph, graphEntityFactory, distanceFactory );
//        assertEquals( expResult, result );
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of preprocess method, of class
     * OptimizedContractionHierarchiesPreprocessor.
     */
    @Test
    public void testPreprocess_5args() {
//        System.out.println( "preprocess" );
//        Graph graph = null;
//        GraphEntityFactory graphEntityFactory = null;
//        DistanceFactory distanceFactory = null;
//        ProgressListener progressListener = null;
//        long startId = 0L;
//        OptimizedContractionHierarchiesPreprocessor instance = new OptimizedContractionHierarchiesPreprocessor();
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> expResult = null;
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> result = instance.preprocess( graph, graphEntityFactory, distanceFactory, progressListener, startId );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of contractNode method, of class
     * OptimizedContractionHierarchiesPreprocessor.
     */
    @Test
    public void testContractNode() {
//        System.out.println( "contractNode" );
//        List<Shortcut> outShortcuts = null;
//        int node = 0;
//        NodeDataStructure<Integer> dijkstraPriorityQueue = null;
//        double[] nodeDistanceArray = null;
//        OptimizedContractionHierarchiesPreprocessor instance = new OptimizedContractionHierarchiesPreprocessor();
//        instance.contractNode( outShortcuts, node, dijkstraPriorityQueue, nodeDistanceArray );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

}
