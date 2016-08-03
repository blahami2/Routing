/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.algorithm.algorithms;

import cz.certicon.routing.application.algorithm.algorithms.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.Route;
import cz.certicon.routing.application.algorithm.RouteBuilder;
import cz.certicon.routing.application.algorithm.RouteNotFoundException;
import cz.certicon.routing.application.algorithm.common.SimpleRouteBuilder;
import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.common.SimpleGraphBuilder;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.GraphBuilder;
import cz.certicon.routing.utils.CoordinateUtils;
import java.util.HashMap;
import java.util.Iterator;
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
public class DijkstraRoutingAlgorithmTest {

    public DijkstraRoutingAlgorithmTest() {
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

    /*        Node a = entityFactory.createNode( Node.Id.createId( 1 ), 50.1001831, 14.3856114 );
        Node b = entityFactory.createNode( Node.Id.createId( 2 ), 50.1002725, 14.3872906 );
        Node c = entityFactory.createNode( Node.Id.createId( 3 ), 50.1018347, 14.3857995 );
        Node d = entityFactory.createNode( Node.Id.createId( 4 ), 50.1017039, 14.3871028 );
        Node e = entityFactory.createNode( Node.Id.createId( 5 ), 50.1002828, 14.3878056 );
        Node f = entityFactory.createNode( Node.Id.createId( 6 ), 50.1016489, 14.3876339 );
        a.setLabel( "a" );
        b.setLabel( "b" );
        c.setLabel( "c" );
        d.setLabel( "d" );
        e.setLabel( "e" );
        f.setLabel( "f" );
        Edge ab = createEdge( entityFactory, distanceFactory, a, b );
        Edge ba = createEdge( entityFactory, distanceFactory, b, a );
        Edge ac = createEdge( entityFactory, distanceFactory, a, c );
        Edge ca = createEdge( entityFactory, distanceFactory, c, a );
        Edge db = createEdge( entityFactory, distanceFactory, d, b );
        Edge cd = createEdge( entityFactory, distanceFactory, c, d );
        Edge dc = createEdge( entityFactory, distanceFactory, d, c );
        Edge be = createEdge( entityFactory, distanceFactory, b, e );
        Edge eb = createEdge( entityFactory, distanceFactory, e, b );
        Edge df = createEdge( entityFactory, distanceFactory, d, f );
        Edge fd = createEdge( entityFactory, distanceFactory, f, d );
        Edge ef = createEdge( entityFactory, distanceFactory, e, f );
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
                .addEdge( ef );*/
    /**
     * Test of route method, of class DijkstraRoutingAlgorithm.
     */
    @Test
    public void testRoute() throws RouteNotFoundException {
        System.out.println( "route" );
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
        Graph graph = graphBuilder.build();
        for ( int i = 0; i < graph.getEdgeCount(); i++ ) {
            System.out.println( "length[" + i + "]: " + graph.getLength( i ) );
        }
        DijkstraRoutingAlgorithm instance = new DijkstraRoutingAlgorithm( graph );
        RouteBuilder<Route, Graph> routeBuilder = new SimpleRouteBuilder();
        routeBuilder.setSourceNode( graph, 1 );
        routeBuilder.addEdgeAsLast( graph, 1 );
        routeBuilder.addEdgeAsLast( graph, 8 );
        routeBuilder.addEdgeAsLast( graph, 12 );
        Route expResult = routeBuilder.build();
        Map<Integer, Float> from = new HashMap<>();
        from.put( 0, 0F );
        Map<Integer, Float> to = new HashMap<>();
        to.put( 5, 0F );
        Route result = instance.route( routeBuilder, from, to );
        assertEquals( toString( graph, expResult ), toString( graph, result ) );
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
