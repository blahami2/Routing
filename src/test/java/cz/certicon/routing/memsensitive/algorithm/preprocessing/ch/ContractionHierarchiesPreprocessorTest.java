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
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
//        GlobalOptions.DEBUG_CORRECTNESS = false;
//        GlobalOptions.DEBUG_TIME = false;
        Properties properties = new Properties();
        properties.setProperty( "driver", "org.sqlite.JDBC" );
        properties.setProperty( "url", "jdbc:sqlite:C:\\Users\\blaha\\Documents\\NetBeansProjects\\RoutingParser\\routing_brandysek.sqlite" );
        properties.setProperty( "spatialite_path", "C:/Routing/Utils/mod_spatialite-4.3.0a-win-amd64/mod_spatialite.dll" );
        DistanceType distanceType = cz.certicon.routing.memsensitive.model.entity.DistanceType.LENGTH;
        GraphReader gr = new SqliteGraphReader( properties );
        Graph graph = gr.readGraph( new SimpleGraphBuilderFactory( cz.certicon.routing.memsensitive.model.entity.DistanceType.LENGTH ), cz.certicon.routing.memsensitive.model.entity.DistanceType.LENGTH );
        ContractionHierarchiesPreprocessor preprocessor = new cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor();
        preprocessor.setNodeRecalculationStrategy( new NeighboursOnlyRecalculationStrategy() );
        preprocessor.setEdgeDifferenceCalculator( new SpatialHeuristicEdgeDifferenceCalculator( graph.getNodeCount() ) );
//
//        System.out.println( "edge count = " + graph.getEdgeCount() );
//        TimeMeasurement time = new TimeMeasurement();
//        time.setTimeUnits( TimeUnits.MILLISECONDS );
//        time.start();
//        PreprocessedData data = preprocessor.preprocess( new SimpleChDataBuilder( graph, distanceType ), graph, distanceType, (long) 10E9 );
//        System.out.println( "Memsensitive preprocessed in: " + time.getCurrentTimeString() );
//        System.out.println( "Created " + data.getShortcutCount() + " shortcuts" );
//
//        cz.certicon.routing.data.DistanceType distanceType2 = cz.certicon.routing.data.DistanceType.LENGTH;
//        GraphEntityFactory graphEntityFactory = new NeighborListGraphEntityFactory();
//        DistanceFactory distanceFactory = distanceType2.getDistanceFactory();
//        cz.certicon.routing.data.graph.GraphReader gr2 = new SqliteGraphRW( properties );
//        gr2.open();
//        cz.certicon.routing.model.entity.Graph graph2 = gr2.read( new Pair<>( graphEntityFactory, distanceFactory ) );
//        gr2.close();
//        OptimizedContractionHierarchiesPreprocessor preprocessor2 = new OptimizedContractionHierarchiesPreprocessor();
//
//        time.start();
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocessedData2 = preprocessor2.preprocess( graph2, graphEntityFactory, distanceFactory );
//        System.out.println( "Object-based preprocessed in: " + time.getCurrentTimeString() );
//        System.out.println( "Created " + preprocessedData2.b.size() + " shortcuts" );

//        System.out.println( "Initialize" );
//        for ( int i = 0; i < graph.getNodeCount(); i++ ) {
//            assertEquals( preprocessor.shortcutCounts.get( i ), preprocessor2.shortcutCounts.get( i ) );
//        }
//        System.out.println( "Computation" );
//        for ( int i = 0; i < preprocessor.shortcutCounts.size(); i++ ) {
//            assertEquals( preprocessor.shortcutCounts.get( i ), preprocessor2.shortcutCounts.get( i ) );
//        }
//        assertEquals( preprocessedData2.b.size(), data.getShortcutCount() );
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> expResult = null;
//        Pair<Map<Node.Id, Integer>, List<Shortcut>> result = instance.preprocess( graph, graphEntityFactory, distanceFactory );
//        System.out.println( "preprocess" );
//        ChDataBuilder<PreprocessedData> dataBuilder = null;
//        Graph graph = null;
//        DistanceType distanceType = null;
//        long startId = 0L;
//        ProgressListener progressListener = null;
//        ContractionHierarchiesPreprocessor instance = new ContractionHierarchiesPreprocessor();
//        PreprocessedData expResult = null;
//        PreprocessedData result = instance.preprocess( dataBuilder, graph, distanceType, startId, progressListener );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
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

}
