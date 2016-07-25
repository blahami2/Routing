/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.preprocessing.ch.OptimizedContractionHierarchiesPreprocessor;
import cz.certicon.routing.data.graph.sqlite.SqliteGraphRW;
import cz.certicon.routing.memsensitive.algorithm.Route;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.ContractionHierarchiesUbRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.DijkstraRoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.common.SimpleRouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators.SpatialHeuristicEdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.NeighboursOnlyRecalculationStrategy;
import cz.certicon.routing.memsensitive.data.graph.GraphReader;
import cz.certicon.routing.memsensitive.data.graph.sqlite.SqliteGraphReader;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeSet;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.memsensitive.model.entity.ch.SimpleChDataBuilder;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilder;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilderFactory;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.entity.neighbourlist.NeighborListGraphEntityFactory;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.model.utility.progress.SimpleProgressListener;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
public class ContractionHierarchiesPreprocessorTest {

    private final Graph graph;

    public ContractionHierarchiesPreprocessorTest() {
        GraphBuilder<Graph> graphBuilder = new SimpleGraphBuilder( 6, 12, DistanceType.LENGTH );
        Coordinate a = new Coordinate( 50.1001831, 14.3856114 );
        Coordinate b = new Coordinate( 50.1002725, 14.3872906 );
        Coordinate c = new Coordinate( 50.1018347, 14.3857995 );
        Coordinate d = new Coordinate( 50.1017039, 14.3871028 );
        Coordinate e = new Coordinate( 50.1002828, 14.3878056 );
        Coordinate f = new Coordinate( 50.1016489, 14.3876339 );
        graphBuilder.addNode( 10, 1, 1, 50.1001831, 14.3856114 );
        graphBuilder.addNode( 20, 2, 2, 50.1002725, 14.3872906 );
        graphBuilder.addNode( 30, 3, 3, 50.1018347, 14.3857995 );
        graphBuilder.addNode( 40, 4, 4, 50.1017039, 14.3871028 );
        graphBuilder.addNode( 50, 5, 5, 50.1002828, 14.3878056 );
        graphBuilder.addNode( 60, 6, 6, 50.1016489, 14.3876339 );
        graphBuilder.addEdge( 1, 1, 1, 10, 20, CoordinateUtils.calculateDistance( a, b ), 50, false );
        graphBuilder.addEdge( 2, 2, 2, 20, 10, CoordinateUtils.calculateDistance( b, a ), 50, false );
        graphBuilder.addEdge( 3, 3, 3, 10, 30, CoordinateUtils.calculateDistance( a, c ), 50, false );
        graphBuilder.addEdge( 4, 4, 4, 30, 10, CoordinateUtils.calculateDistance( c, a ), 50, false );
        graphBuilder.addEdge( 5, 5, 5, 40, 20, CoordinateUtils.calculateDistance( d, b ), 50, false );
        graphBuilder.addEdge( 6, 6, 6, 30, 40, CoordinateUtils.calculateDistance( c, d ), 50, false );
        graphBuilder.addEdge( 7, 7, 7, 40, 30, CoordinateUtils.calculateDistance( d, c ), 50, false );
        graphBuilder.addEdge( 8, 8, 8, 20, 50, CoordinateUtils.calculateDistance( b, e ), 50, false );
        graphBuilder.addEdge( 9, 9, 9, 50, 20, CoordinateUtils.calculateDistance( e, b ), 50, false );
        graphBuilder.addEdge( 10, 10, 10, 40, 60, CoordinateUtils.calculateDistance( d, f ), 50, false );
        graphBuilder.addEdge( 11, 11, 11, 60, 40, CoordinateUtils.calculateDistance( f, d ), 50, false );
        graphBuilder.addEdge( 12, 12, 12, 50, 60, CoordinateUtils.calculateDistance( e, f ), 50, false );
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
     * Test of setNodeRecalculationStrategy method, of class
     * ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testSetNodeRecalculationStrategy() {
        System.out.println( "setNodeRecalculationStrategy" );
    }

    /**
     * Test of preprocess method, of class ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testPreprocess_4args() throws RouteNotFoundException {
        System.out.println( "preprocess" );
        DistanceType distanceType = DistanceType.LENGTH;
        ChDataBuilder<PreprocessedData> dataBuilder = new SimpleChDataBuilder( graph, distanceType );
        long startId = 12L;
        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
        SimpleChDataBuilder pdBuilder = new SimpleChDataBuilder( graph, distanceType );
        pdBuilder.setRank( 10, 1 );
        pdBuilder.setRank( 20, 2 );
        pdBuilder.setRank( 30, 6 );
        pdBuilder.setRank( 40, 4 );
        pdBuilder.setRank( 50, 5 );
        pdBuilder.setRank( 60, 3 );
        pdBuilder.addShortcut( 13, 2, 3 );
        pdBuilder.addShortcut( 14, 5, 8 );
        pdBuilder.addShortcut( 15, 12, 11 );
        pdBuilder.addShortcut( 16, 6, 14 );
        pdBuilder.addShortcut( 17, 15, 7 );
//        PreprocessedData expResult = pdBuilder.build();
        PreprocessedData preprocessedData = instance.preprocess( dataBuilder, graph, distanceType, startId );
//        System.out.println( expResult );
//        System.out.println( preprocessedData );
//        assertEquals( expResult, preprocessedData );

        System.out.println( "incoming shortcuts:" );
        for ( int[] incomingShortcut : preprocessedData.getIncomingShortcuts() ) {
            for ( int i : incomingShortcut ) {
                System.out.print( i + " => ( " + preprocessedData.getSource( i ) + " -> " + preprocessedData.getTarget( i ) + "), " );
            }
            System.out.println( "" );
        }
        System.out.println( "outgoing shortcuts:" );
        for ( int[] outgoingShortcut : preprocessedData.getOutgoingShortcuts() ) {
            for ( int i : outgoingShortcut ) {
                System.out.print( i + " => ( " + preprocessedData.getSource( i ) + " -> " + preprocessedData.getTarget( i ) + "), " );
            }
            System.out.println( "" );
        }
        System.out.println( "ranks: " );
        int nodeIdx = 0;
        for ( int rank : preprocessedData.getRanks() ) {
            System.out.println( "#" + nodeIdx++ + " -> " + rank );
        }
        System.out.println( "trs: " + preprocessedData.getTurnRestrictions() );

        DijkstraRoutingAlgorithm optimalAlgorithm = new DijkstraRoutingAlgorithm( graph );
        ContractionHierarchiesUbRoutingAlgorithm chAlgorithm = new ContractionHierarchiesUbRoutingAlgorithm( graph, preprocessedData );
        for ( int i = 0; i < 6; i++ ) {
            for ( int j = 0; j < 6; j++ ) {
                if ( i != j ) {
                    Map<Integer, RoutingAlgorithm.NodeEntry> from = new HashMap<>();
                    from.put( i, new RoutingAlgorithm.NodeEntry( -1, i, 0F ) );
                    Map<Integer, RoutingAlgorithm.NodeEntry> to = new HashMap<>();
                    to.put( j, new RoutingAlgorithm.NodeEntry( -1, j, 0F ) );
                    Route expResult = optimalAlgorithm.route( new SimpleRouteBuilder(), from, to );
                    Route result = chAlgorithm.route( new SimpleRouteBuilder(), from, to );
                    assertEquals( toString( graph, expResult ), toString( graph, result ) );
                }
            }
        }
//        fail();
    }

    /**
     * Test of preprocess method, of class ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testPreprocess_5args() throws IOException {
        DistanceType distanceType = DistanceType.LENGTH;
        ChDataBuilder<PreprocessedData> dataBuilder = new SimpleChDataBuilder( graph, distanceType );
        long startId = 12L;
        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
        int[][][] tr = new int[graph.getNodeCount()][][];
        tr[3] = new int[1][2];
        tr[3][0][0] = 10;
        tr[3][0][1] = 4;
        graph.setTurnRestrictions( tr );
        PreprocessedData preprocessedData = instance.preprocess( dataBuilder, graph, distanceType, startId );
        System.out.println( "incoming shortcuts:" );
        for ( int[] incomingShortcut : preprocessedData.getIncomingShortcuts() ) {
            for ( int i : incomingShortcut ) {
                System.out.print( i + " => ( " + preprocessedData.getSource( i ) + " -> " + preprocessedData.getTarget( i ) + "; " + preprocessedData.getStartEdge( i ) + " -> " + preprocessedData.getEndEdge( i ) + "), " );
            }
            System.out.println( "" );
        }
        System.out.println( "outgoing shortcuts:" );
        for ( int[] outgoingShortcut : preprocessedData.getOutgoingShortcuts() ) {
            for ( int i : outgoingShortcut ) {
                System.out.print( i + " => ( " + preprocessedData.getSource( i ) + " -> " + preprocessedData.getTarget( i ) + "; " + preprocessedData.getStartEdge( i ) + " -> " + preprocessedData.getEndEdge( i ) + "), " );
            }
            System.out.println( "" );
        }
        System.out.println( "ranks: " );
        int nodeIdx = 0;
        for ( int rank : preprocessedData.getRanks() ) {
            System.out.println( "#" + nodeIdx++ + " -> " + rank );
        }
        print( preprocessedData.getTurnRestrictions() );
//        fail();
    }

    public String toString( Graph graph, Route route ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "(" );
        Iterator<Pair<Long, Boolean>> edgeIterator = route.getEdgeIterator();
        while ( edgeIterator.hasNext() ) {
            Pair<Long, Boolean> next = edgeIterator.next();
//            System.out.println( next );
            sb.append( "(" )
                    .append( graph.getNodeOrigId( graph.getSource( graph.getEdgeByOrigId( next.a ) ) ) )
                    .append( " " )
                    .append( graph.getNodeOrigId( graph.getTarget( graph.getEdgeByOrigId( next.a ) ) ) )
                    .append( "), " );
//            System.out.println( sb.toString() );
        }
        sb.delete( sb.length() - 3, sb.length() );
        sb.append( ")" );
        return sb.toString();
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

    /**
     * Test of setEdgeDifferenceCalculator method, of class
     * ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testSetEdgeDifferenceCalculator() {
        System.out.println( "setEdgeDifferenceCalculator" );
//        EdgeDifferenceCalculator edgeDifferenceCalculator = null;
//        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
//        instance.setEdgeDifferenceCalculator( edgeDifferenceCalculator );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of calculateTurns method, of class
     * ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testCalculateTurns() {
        System.out.println( "calculateTurns" );
        DistanceType distanceType = DistanceType.LENGTH;
        ContractionHierarchiesPreprocessor preprocessor = new ContractionHierarchiesPreprocessor();
        int[][][] tr = new int[graph.getNodeCount()][][];
        tr[3] = new int[1][2];
        tr[3][0][0] = 5;
        tr[3][0][1] = 4;
        graph.setTurnRestrictions( tr );
        ProcessingData data = new ProcessingData( graph );
        int shortcut = preprocessor.calculateTurns( data, graph, 3, 5, 4 );
        assertNotEquals( -1, shortcut );
        graph.setTurnRestrictions( null );
    }

    /**
     * Test of createShortcuts method, of class
     * ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testCreateShortcuts() {
        System.out.println( "createShortcuts" );
//        LinkedList<Integer> previousLayer = null;
//        ProcessingData data = null;
//        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
//        LinkedList<Integer> expResult = null;
//        LinkedList<Integer> result = instance.createShortcuts( previousLayer, data );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of calculateShortcuts method, of class
     * ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testCalculateShortcuts() {
        System.out.println( "calculateShortcuts" );
//        ProcessingData data = null;
//        BitArray removedNodes = null;
//        int node = 0;
//        Graph graph = null;
//        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
//        int expResult = 0;
//        int result = instance.calculateShortcuts( data, removedNodes, node, graph );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of contractNode method, of class ContractionHierarchiesPreprocessor.
     */
    @Test
    public void testContractNode() {
        System.out.println( "contractNode" );
//        ProcessingData data = null;
//        int[] nodeDegrees = null;
//        BitArray removedNodes = null;
//        int node = 0;
//        Graph graph = null;
//        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
//        instance.contractNode( data, nodeDegrees, removedNodes, node, graph );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }
}
