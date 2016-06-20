/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.ch;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilder;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.Arrays;
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
public class SimpleChDataBuilderTest {

    Graph graph;

    public SimpleChDataBuilderTest() {
        GraphBuilder<Graph> graphBuilder = new SimpleGraphBuilder( 6, 12, DistanceType.LENGTH );
        Coordinate a = new Coordinate( 50.1001831, 14.3856114 );
        Coordinate b = new Coordinate( 50.1002725, 14.3872906 );
        Coordinate c = new Coordinate( 50.1018347, 14.3857995 );
        Coordinate d = new Coordinate( 50.1017039, 14.3871028 );
        Coordinate e = new Coordinate( 50.1002828, 14.3878056 );
        Coordinate f = new Coordinate( 50.1016489, 14.3876339 );
        graphBuilder.addNode( 1, 1, 1, 50.1001831, 14.3856114 );
        graphBuilder.addNode( 2, 2, 2, 50.1002725, 14.3872906 );
        graphBuilder.addNode( 3, 3, 3, 50.1018347, 14.3857995 );
        graphBuilder.addNode( 4, 4, 4, 50.1017039, 14.3871028 );
        graphBuilder.addNode( 5, 5, 5, 50.1002828, 14.3878056 );
        graphBuilder.addNode( 6, 6, 6, 50.1016489, 14.3876339 );
        graphBuilder.addEdge( 1, 1, 1, 1, 2, CoordinateUtils.calculateDistance( a, b ), 50, false );
        graphBuilder.addEdge( 2, 2, 2, 2, 1, CoordinateUtils.calculateDistance( b, a ), 50, false );
        graphBuilder.addEdge( 3, 3, 3, 1, 3, CoordinateUtils.calculateDistance( a, c ), 50, false );
        graphBuilder.addEdge( 4, 4, 4, 3, 1, CoordinateUtils.calculateDistance( c, a ), 50, false );
        graphBuilder.addEdge( 5, 5, 5, 4, 2, CoordinateUtils.calculateDistance( d, b ), 50, false );
        graphBuilder.addEdge( 6, 6, 6, 3, 4, CoordinateUtils.calculateDistance( c, d ), 50, false );
        graphBuilder.addEdge( 7, 7, 7, 4, 3, CoordinateUtils.calculateDistance( d, c ), 50, false );
        graphBuilder.addEdge( 8, 8, 8, 2, 5, CoordinateUtils.calculateDistance( b, e ), 50, false );
        graphBuilder.addEdge( 9, 9, 9, 5, 2, CoordinateUtils.calculateDistance( e, b ), 50, false );
        graphBuilder.addEdge( 10, 10, 10, 4, 6, CoordinateUtils.calculateDistance( d, f ), 50, false );
        graphBuilder.addEdge( 11, 11, 11, 6, 4, CoordinateUtils.calculateDistance( f, d ), 50, false );
        graphBuilder.addEdge( 12, 12, 12, 5, 6, CoordinateUtils.calculateDistance( e, f ), 50, false );
        graph = graphBuilder.build();
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
     * Test of setRank method, of class SimpleChDataBuilder.
     */
    @Test
    public void testSetRank() {
        System.out.println( "setRank" );
        long nodeId = 1L;
        int rank = 0;
        SimpleChDataBuilder instance = new SimpleChDataBuilder( graph, DistanceType.LENGTH );
        instance.setRank( nodeId, rank );
        PreprocessedData build = instance.build();
        assertEquals( build.getRank( 0 ), 0 );
    }

    /**
     * Test of addShortcut method, of class SimpleChDataBuilder.
     */
    @Test
    public void testAddShortcut() {
        System.out.println( "addShortcut" );
        SimpleChDataBuilder instance = new SimpleChDataBuilder( graph, DistanceType.LENGTH );
        instance.addShortcut( 14, 5, 8 );
        instance.addShortcut( 15, 6, 14 );
        PreprocessedData build = instance.build();
        assertEquals( toString( new int[]{ 0, 1 } ), toString( build.getIncomingShortcuts( 4 ) ) );
        assertEquals( toString( new int[]{ 0 } ), toString( build.getOutgoingShortcuts( 3 ) ) );
    }

    /**
     * Test of getDistanceTypeIntValue method, of class SimpleChDataBuilder.
     */
    @Test
    public void testGetDistanceTypeIntValue() {
        System.out.println( "getDistanceTypeIntValue" );
    }

    /**
     * Test of build method, of class SimpleChDataBuilder.
     */
    @Test
    public void testBuild() {
        System.out.println( "build" );
        SimpleChDataBuilder instance = null;
        SimpleChDataBuilder pdBuilder = new SimpleChDataBuilder( graph, DistanceType.LENGTH );
        pdBuilder.setRank( 1, 1 );
        pdBuilder.setRank( 2, 2 );
        pdBuilder.setRank( 3, 6 );
        pdBuilder.setRank( 4, 4 );
        pdBuilder.setRank( 5, 5 );
        pdBuilder.setRank( 6, 3 );
        pdBuilder.addShortcut( 13, 2, 3 );
        pdBuilder.addShortcut( 14, 5, 8 );
        pdBuilder.addShortcut( 15, 12, 11 );
        pdBuilder.addShortcut( 16, 6, 14 );
        pdBuilder.addShortcut( 17, 15, 7 );
        PreprocessedData result = pdBuilder.build();

        PreprocessedData expResult = new PreprocessedData(
                new int[]{ 1, 2, 6, 4, 5, 3 },
                new int[][]{ {}, {}, { 4, 0 }, { 2 }, { 3, 1 }, {} },
                new int[][]{ {}, { 0 }, { 3 }, { 1 }, { 4, 2 }, {} },
                new int[]{ 1, 3, 4, 2, 4 },
                new int[]{ 2, 4, 3, 4, 2 },
                new int[]{ 1, 4, 11, 5, 14 },
                new int[]{ 2, 7, 10, 13, 6 },
                0 );
//        System.out.println( expResult );
//        System.out.println( result );
        assertEquals( expResult, result );
    }

    public String toString( int[] array ) {
        return Arrays.toString( array );
    }

}
