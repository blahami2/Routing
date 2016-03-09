/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.jgrapht.JgraphtDirectedGraphEntityFactory;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighbourListGraphEntityFactory;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
@RunWith( Parameterized.class )
public class DirectedGraphTest {

    private final int LOOPS = 10;

    static final LengthDistanceFactory distanceFactory = new LengthDistanceFactory();
    private final GraphEntityFactory graphFactory;

    public DirectedGraphTest( GraphEntityFactory graphFactory ) {
        this.graphFactory = graphFactory;
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
     * Test of addNode method, of class Graph.
     */
    @Test
    public void testAddNode() {
        System.out.println( "addNode" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node node = graphFactory.createNode( Node.Id.generateId(), LOOPS + 1, LOOPS + 1 );
        assertFalse( containsNode( instance, node ) );
        assertEquals( LOOPS * LOOPS, instance.getNodes().size() );
        instance.addNode( node );
        assertTrue( containsNode( instance, node ) );
        assertEquals( LOOPS * LOOPS + 1, instance.getNodes().size() );
    }

    /**
     * Test of removeNode method, of class Graph.
     */
    @Test
    public void testRemoveNode() {
        System.out.println( "removeNode" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node node = findNode( instance, LOOPS - 1, LOOPS - 1 );
        assertTrue( containsNode( instance, node ) );
        assertEquals( LOOPS * LOOPS, instance.getNodes().size() );
        instance.removeNode( node );
        assertFalse( containsNode( instance, node ) );
        assertEquals( LOOPS * LOOPS - 1, instance.getNodes().size() );
    }

    /**
     * Test of addEdge method, of class Graph.
     */
    @Test
    public void testAddEdge() {
        System.out.println( "addEdge" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        assertFalse( containsEdge( instance, edge ) );
        instance.addEdge( a, b, edge );
        assertTrue( containsEdge( instance, edge ) );
    }

    /**
     * Test of addEdge method, of class Graph.
     */
    @Test
    public void testAddEdge_Edge() {
        System.out.println( "addEdge" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addNode( a ).addNode( b );
        assertFalse( containsEdge( instance, edge ) );
        instance.addEdge( edge );
        assertTrue( containsEdge( instance, edge ) );
    }

    /**
     * Test of addEdge method, of class Graph.
     */
    @Test
    public void testAddEdge_3args() {
        System.out.println( "addEdge" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        assertFalse( containsEdge( instance, edge ) );
        instance.addEdge( a, b, edge );
        assertTrue( containsEdge( instance, edge ) );
    }

    /**
     * Test of removeEdge method, of class Graph.
     */
    @Test
    public void testRemoveEdge_Edge() {
        System.out.println( "removeEdge" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        instance.addNode( a ).addNode( b );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( a, b, edge );
        assertTrue( containsEdge( instance, edge ) );
        instance.removeEdge( edge );
        assertFalse( containsEdge( instance, edge ) );
    }

    /**
     * Test of getSourceNodeOf method, of class Graph.
     */
    @Test
    public void testGetSourceNodeOf() {
        System.out.println( "getSourceNodeOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        instance.addNode( a ).addNode( b );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( a, instance.getSourceNodeOf( edge ) );
    }

    /**
     * Test of getTargetNodeOf method, of class Graph.
     */
    @Test
    public void testGetTargetNodeOf() {
        System.out.println( "getTargetNodeOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        instance.addNode( a ).addNode( b );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( b, instance.getTargetNodeOf( edge ) );
    }

    /**
     * Test of getEdgesOf method, of class Graph.
     */
    @Test
    public void testGetEdgesOf() {
        System.out.println( "getEdgesOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( new HashSet<Edge>( Arrays.asList( edge ) ),
                instance.getEdgesOf( a ) );
        assertEquals( new HashSet<Edge>( Arrays.asList( edge ) ),
                instance.getEdgesOf( b ) );
    }

    /**
     * Test of getIncomingEdgesOf method, of class Graph.
     */
    @Test
    public void testGetIncomingEdgesOf() {
        System.out.println( "getIncomingEdgesOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        Set<Edge> expResult = new HashSet<>();
        assertEquals( expResult,
                instance.getIncomingEdgesOf( a ) );
        expResult.add( edge );
        assertEquals( expResult,
                instance.getIncomingEdgesOf( b ) );
    }

    /**
     * Test of getOutgoingEdgesOf method, of class Graph.
     */
    @Test
    public void testGetOutgoingEdgesOf() {
        System.out.println( "getOutgoingEdgesOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        Set<Edge> expResult = new HashSet<>();
        assertEquals( expResult,
                instance.getOutgoingEdgesOf( b ) );
        expResult.add( edge );
        assertEquals( expResult,
                instance.getOutgoingEdgesOf( a ) );
    }

    /**
     * Test of getDegreeOf method, of class Graph.
     */
    @Test
    public void testGetDegreeOf() {
        System.out.println( "getDegreeOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( 1, instance.getDegreeOf( a ) );
        assertEquals( 1, instance.getDegreeOf( b ) );
    }

    /**
     * Test of getInDegreeOf method, of class Graph.
     */
    @Test
    public void testGetInDegreeOf() {
        System.out.println( "getInDegreeOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( 0, instance.getInDegreeOf( a ) );
        assertEquals( 1, instance.getInDegreeOf( b ) );
    }

    /**
     * Test of getOutDegreeOf method, of class Graph.
     */
    @Test
    public void testGetOutDegreeOf() {
        System.out.println( "getOutDegreeOf" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( 1, instance.getOutDegreeOf( a ) );
        assertEquals( 0, instance.getOutDegreeOf( b ) );
    }

    /**
     * Test of getNodes method, of class Graph.
     */
    @Test
    public void testGetNodes() {
        System.out.println( "getNodes" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Set<Node> expResult = new HashSet<>();
        for ( int i = 0; i < LOOPS; i++ ) {
            for ( int j = 0; j < LOOPS; j++ ) {
                expResult.add( findNode( instance, i, j ) );
            }
        }
        Set<Node> result = instance.getNodes();
        assertEquals( expResult, result );
    }

    /**
     * Test of getEdges method, of class Graph.
     */
    @Test
    public void testGetEdges() {
        System.out.println( "getEdges" );
        Graph instance = initGraph( graphFactory, LOOPS );
        Node a = findNode( instance, LOOPS - 1, 0 );
        Node b = findNode( instance, 0, LOOPS - 1 );
        Edge edge = createEdge( graphFactory, distanceFactory, a, b, true );
        instance.addEdge( edge );
        assertEquals( new HashSet<Edge>( Arrays.asList( edge ) ),
                instance.getEdges() );
    }

    private static Edge createEdge( GraphEntityFactory entityFactory, DistanceFactory distanceFactory, Node sourceNode, Node targetNode, boolean oneWay ) {
        EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder( 50 ).setOneWay( oneWay ).setLength( CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) ).build();
        return entityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode, distanceFactory.createFromEdgeAttributes( edgeAttributes ) )
                .setAttributes( edgeAttributes );
    }

    private static Node findNode( Graph g, double latitude, double longitude ) {
        Coordinate c = new Coordinate( latitude, longitude );
        for ( Node n : g.getNodes() ) {
            if ( n.getCoordinates().equals( c ) ) {
                return n;
            }
        }
        return null;
    }

    private static boolean containsNode( Graph g, Node n ) {
        for ( Node node : g.getNodes() ) {
            if ( node.equals( n ) ) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsEdge( Graph g, Edge e ) {
        for ( Edge edge : g.getEdges() ) {
            if ( edge.equals( e ) ) {
                return true;
            }
        }
        return false;
    }

    private static Graph initGraph( GraphEntityFactory factory, int loops ) {
        Graph instance = factory.createGraph();
        for ( int i = 0; i < loops; i++ ) {
            for ( int j = 0; j < loops; j++ ) {
                instance.addNode( factory.createNode( Node.Id.generateId(), i, j ) );
            }
        }
        return instance;
    }

    private static Graph initGraphWithEdges( GraphEntityFactory factory, int loops ) {
        Graph instance = initGraph( factory, loops );
        for ( Node a : instance.getNodes() ) {
            for ( Node b : instance.getNodes() ) {
                if ( !a.equals( b ) ) {
                    instance.addEdge( createEdge( factory, distanceFactory, a, b, true ) );
                }
            }
        }
        return instance;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> instancesToTest() {
        return Arrays.asList(
                new Object[]{
                    new JgraphtDirectedGraphEntityFactory()
                },
                new Object[]{
                    new DirectedNeighbourListGraphEntityFactory()
                }
        );
    }

}
