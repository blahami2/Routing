/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.utils.CoordinateUtils;
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
public class SimpleTurnTablesBuilderTest {

    public SimpleTurnTablesBuilderTest() {
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
     * Test of addRestriction method, of class SimpleTurnTablesBuilder.
     */
    @Test
    public void testAddRestriction_6args() {
        System.out.println( "addRestriction" );
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
        graphBuilder.addNode( 259, 259, 259, 50.1016489, 14.3876339 );
        graphBuilder.addEdge( 1, 1, 1, 1, 2, CoordinateUtils.calculateDistance( a, b ), 50, false );
        graphBuilder.addEdge( 2, 2, 2, 2, 1, CoordinateUtils.calculateDistance( b, a ), 50, false );
        graphBuilder.addEdge( 3, 3, 3, 1, 3, CoordinateUtils.calculateDistance( a, c ), 50, false );
        graphBuilder.addEdge( 4, 4, 4, 3, 1, CoordinateUtils.calculateDistance( c, a ), 50, false );
        graphBuilder.addEdge( 5, 5, 5, 4, 2, CoordinateUtils.calculateDistance( d, b ), 50, false );
        graphBuilder.addEdge( 6, 6, 6, 3, 4, CoordinateUtils.calculateDistance( c, d ), 50, false );
        graphBuilder.addEdge( 7, 7, 7, 4, 3, CoordinateUtils.calculateDistance( d, c ), 50, false );
        graphBuilder.addEdge( 8, 8, 8, 2, 5, CoordinateUtils.calculateDistance( b, e ), 50, false );
        graphBuilder.addEdge( 9, 9, 9, 5, 2, CoordinateUtils.calculateDistance( e, b ), 50, false );
        graphBuilder.addEdge( 20609L, 20609L, 20609L, 4, 259, CoordinateUtils.calculateDistance( d, f ), 50, false );
        graphBuilder.addEdge( 7325L, 7325L, 7325L, 259, 4, CoordinateUtils.calculateDistance( f, d ), 50, false );
        graphBuilder.addEdge( 20610L, 20610L, 20610L, 5, 259, CoordinateUtils.calculateDistance( e, f ), 50, false );
        Graph graph = graphBuilder.build();
        int arrayId = 498;
        long from = 20609L;
        int fromPosition = 0;
        long via = 259L;
        long to = 7325L;
        SimpleTurnTablesBuilder instance = new SimpleTurnTablesBuilder();
        instance.addRestriction( graph, arrayId, 20609, fromPosition, via, to );
        instance.addRestriction( graph, arrayId, 20610, fromPosition, via, to );

        int[][][] tr = new int[graph.getNodeCount()][][];
        tr[5] = new int[1][2];
        tr[5][0][0] = 9;
        tr[5][0][1] = 10;
//        tr[5][1][0] = 11;
//        tr[5][1][1] = 10;
        int[][][] tr1 = instance.build( graph ).getTr();
        print( tr );
        System.out.println( "==================" );
        print( tr1 );
        assertTrue( compare( tr, tr1 ) );
    }

    /**
     * Test of addRestriction method, of class SimpleTurnTablesBuilder.
     */
    @Test
    public void testAddRestriction_7args() {
        System.out.println( "addRestriction" );
//        Graph graph = null;
//        PreprocessedData chData = null;
//        int arrayId = 0;
//        long from = 0L;
//        int fromPosition = 0;
//        long via = 0L;
//        long to = 0L;
//        SimpleTurnTablesBuilder instance = new SimpleTurnTablesBuilder();
//        instance.addRestriction( graph, chData, arrayId, from, fromPosition, via, to );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of build method, of class SimpleTurnTablesBuilder.
     */
    @Test
    public void testBuild_Graph() {
//        System.out.println( "build" );
//        Graph graph = null;
//        SimpleTurnTablesBuilder instance = new SimpleTurnTablesBuilder();
//        SimpleTurnTablesBuilder.TurnTablesContainer expResult = null;
//        SimpleTurnTablesBuilder.TurnTablesContainer result = instance.build( graph );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of build method, of class SimpleTurnTablesBuilder.
     */
    @Test
    public void testBuild_Graph_PreprocessedData() {
//        System.out.println( "build" );
//        Graph graph = null;
//        PreprocessedData chData = null;
//        SimpleTurnTablesBuilder instance = new SimpleTurnTablesBuilder();
//        SimpleTurnTablesBuilder.TurnTablesContainer expResult = null;
//        SimpleTurnTablesBuilder.TurnTablesContainer result = instance.build( graph, chData );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    private static boolean compare( int[][][] expected, int[][][] actual ) {
        if ( expected.length != actual.length ) {
            System.out.println( "length-e: " + expected.length + "; a: " + actual.length );
            return false;
        }
        for ( int i = 0; i < actual.length; i++ ) {
            if ( expected[i] == null ) {
                if ( actual[i] != null ) {
                    System.out.println( "[" + i + "]-e: null; a: " + actual[i].length );
                    return false;
                }
            } else {
                if ( expected[i].length != actual[i].length ) {
                    System.out.println( "[" + i + "]-length-e: " + expected[i].length + "; a: " + actual[i].length );
                    return false;
                }
                for ( int j = 0; j < actual[i].length; j++ ) {
                    if ( expected[i][j].length != actual[i][j].length ) {
                        System.out.println( "[" + i + "][" + j + "]-length-e: " + expected[i][j].length + "; a: " + actual[i][j].length );
                        return false;
                    }
                    for ( int k = 0; k < actual[i][j].length; k++ ) {
                        if ( expected[i][j][k] != actual[i][j][k] ) {
                            System.out.println( "[" + i + "][" + j + "][" + k + "]-e: " + expected[i][j][k] + "; a: " + actual[i][j][k] );
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void print( int[][][] array ) {
        for ( int i = 0; i < array.length; i++ ) {
            System.out.print( i );
            if ( array[i] != null ) {
                for ( int j = 0; j < array[i].length; j++ ) {
                    System.out.print( "\t" + j + " -> " );
                    for ( int k = 0; k < array[i][j].length; k++ ) {
                        System.out.print( array[i][j][k] + " " );
                    }
                    System.out.println( "" );
                }
            } else {
                System.out.println( "" );
            }
        }
    }

}
