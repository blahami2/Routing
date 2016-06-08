/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.neighbourlist;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilder;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.efficient.BitArray;
import gnu.trove.iterator.TIntIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class NeighbourlistGraphTest {

    public NeighbourlistGraphTest() {

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
     * Test of setSource method, of class NeighbourlistGraph.
     */
    @Test
    public void testSetSource() {
        System.out.println( "setSource" );
        int edge = 2;
        int source = 1;
        Graph instance = createGraph();
        instance.setSource( edge, source );
        assertEquals( instance.getSource( edge ), source );
    }

    /**
     * Test of setTarget method, of class NeighbourlistGraph.
     */
    @Test
    public void testSetTarget() {
        System.out.println( "setTarget" );
        int edge = 2;
        int target = 2;
        Graph instance = createGraph();
        instance.setTarget( edge, target );
        assertEquals( instance.getTarget( edge ), target );
    }

    /**
     * Test of setLength method, of class NeighbourlistGraph.
     */
    @Test
    public void testSetLength() {
        System.out.println( "setLength" );
        int edge = 0;
        float length = 15.0F;
        Graph instance = createGraph();
        instance.setLength( edge, length );
        assertEquals( instance.getLength( edge ), length, 0.0000001 );
    }

    /**
     * Test of setIncomingEdges method, of class NeighbourlistGraph.
     */
    @Test
    public void testSetIncomingEdges() {
        System.out.println( "setIncomingEdges" );
        int node = 0;
        int[] incomingEdges = { 1, 2, 3 };
        Graph instance = createGraph();
        instance.setIncomingEdges( node, incomingEdges );
        Assert.assertArrayEquals( instance.getIncomingEdges( node ), incomingEdges );
    }

    /**
     * Test of getNodeDegree method, of class NeighbourlistGraph.
     */
    @Test
    public void testGetNodeDegree() {
        System.out.println( "getNodeDegree" );
        int node = 0;
        Graph instance = createGraph();
        int expResult = 4;
        int result = instance.getNodeDegree( node );
        assertEquals( expResult, result );
    }

    /**
     * Test of isValidWay method, of class NeighbourlistGraph.
     */
    @Test
    public void testIsValidWay() {
        System.out.println( "isValidWay" );
        int node = 0;
        int targetEdge = 2;
        Graph instance = createGraph();
        int[][][] turnRestrictions = { { { 4, 1, 2 } } };
        instance.setTurnRestrictions( turnRestrictions );
        int[] predecessorArray = { 1, 4, -1, -1, -1, -1 };
        assertFalse( instance.isValidWay( node, targetEdge, predecessorArray ) );
        int[] predecessorArray2 = { 1, 8, -1, -1, -1, -1 };
        assertTrue( instance.isValidWay( node, targetEdge, predecessorArray2 ) );
    }

    private Graph createGraph() {
        GraphBuilder<Graph> graphBuilder = new SimpleGraphBuilder( 6, 12, DistanceType.LENGTH );
        Coordinate a = new Coordinate( 50.1001831, 14.3856114 );
        Coordinate b = new Coordinate( 50.1002725, 14.3872906 );
        Coordinate c = new Coordinate( 50.1018347, 14.3857995 );
        Coordinate d = new Coordinate( 50.1017039, 14.3871028 );
        Coordinate e = new Coordinate( 50.1002828, 14.3878056 );
        Coordinate f = new Coordinate( 50.1016489, 14.3876339 );
        graphBuilder.addNode( 0, 1, 1, 50.1001831, 14.3856114 );
        graphBuilder.addNode( 1, 2, 2, 50.1002725, 14.3872906 );
        graphBuilder.addNode( 2, 3, 3, 50.1018347, 14.3857995 );
        graphBuilder.addNode( 3, 4, 4, 50.1017039, 14.3871028 );
        graphBuilder.addNode( 4, 5, 5, 50.1002828, 14.3878056 );
        graphBuilder.addNode( 5, 6, 6, 50.1016489, 14.3876339 );
        graphBuilder.addEdge( 0, 1, 1, 0, 1, CoordinateUtils.calculateDistance( a, b ), 50, false );
        graphBuilder.addEdge( 1, 2, 2, 1, 0, CoordinateUtils.calculateDistance( b, a ), 50, false );
        graphBuilder.addEdge( 2, 3, 3, 0, 2, CoordinateUtils.calculateDistance( a, c ), 50, false );
        graphBuilder.addEdge( 3, 4, 4, 2, 0, CoordinateUtils.calculateDistance( c, a ), 50, false );
        graphBuilder.addEdge( 4, 5, 5, 3, 1, CoordinateUtils.calculateDistance( d, b ), 50, false );
        graphBuilder.addEdge( 5, 6, 6, 2, 3, CoordinateUtils.calculateDistance( c, d ), 50, false );
        graphBuilder.addEdge( 6, 7, 7, 3, 2, CoordinateUtils.calculateDistance( d, c ), 50, false );
        graphBuilder.addEdge( 7, 8, 8, 1, 4, CoordinateUtils.calculateDistance( b, e ), 50, false );
        graphBuilder.addEdge( 8, 9, 9, 4, 1, CoordinateUtils.calculateDistance( e, b ), 50, false );
        graphBuilder.addEdge( 9, 10, 10, 3, 5, CoordinateUtils.calculateDistance( d, f ), 50, false );
        graphBuilder.addEdge( 10, 11, 11, 5, 3, CoordinateUtils.calculateDistance( f, d ), 50, false );
        graphBuilder.addEdge( 11, 12, 12, 4, 5, CoordinateUtils.calculateDistance( e, f ), 50, false );
        return graphBuilder.build();
    }

}
