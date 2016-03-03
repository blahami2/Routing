/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.algorithm.interfaces;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.NodeEvaluator;
import cz.certicon.routing.application.algorithm.AlgorithmConfiguration;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.RoutingConfiguration;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.data.number.DoubleDistanceFactory;
import cz.certicon.routing.application.algorithm.datastructures.TrivialNodeDataStructure;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighbourListGraphEntityFactory;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
@RunWith( Parameterized.class )
public class RoutingAlgorithmTest {

    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final DirectedNeighbourListGraphEntityFactory graphFactory;
    private final DoubleDistanceFactory distanceFactory;
    private final Graph graph;

    public RoutingAlgorithmTest( RoutingAlgorithmFactory routingAlgorithmFactory1 ) {
        this.routingAlgorithmFactory = routingAlgorithmFactory1;
        this.graph = routingAlgorithmFactory1.getGraph();
        this.graphFactory = new DirectedNeighbourListGraphEntityFactory();
        this.distanceFactory = new DoubleDistanceFactory();
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
     * Test of route method, of class RoutingAlgorithm.
     */
    @Test
    public void testRoute() throws Exception {
        System.out.println( "route" );
        Node a = graphFactory.createNode( Node.Id.generateId(), 50.1001831, 14.3856114 );
        Node b = graphFactory.createNode( Node.Id.generateId(), 50.1002725, 14.3872906 );
        Node c = graphFactory.createNode( Node.Id.generateId(), 50.1018347, 14.3857995 );
        Node d = graphFactory.createNode( Node.Id.generateId(), 50.1017039, 14.3871028 );
        Node e = graphFactory.createNode( Node.Id.generateId(), 50.1002828, 14.3878056 );
        Node f = graphFactory.createNode( Node.Id.generateId(), 50.1016489, 14.3876339 );
        RoutingAlgorithm instance = routingAlgorithmFactory.createRoutingAlgorithm();
        Path expResult;

        Graph g = graphFactory.createGraph();
        Edge ab = createEdge( graphFactory, distanceFactory, a, b, false );
        Edge ac = createEdge( graphFactory, distanceFactory, a, c, false );
        Edge be = createEdge( graphFactory, distanceFactory, b, e, false );
        Edge cd = createEdge( graphFactory, distanceFactory, c, d, false );
        Edge df = createEdge( graphFactory, distanceFactory, d, f, false );
        Edge ef = createEdge( graphFactory, distanceFactory, e, f, true );
        g.addNode( a ).addNode( b ).addNode( c ).addNode( d ).addNode( e ).addNode( f )
                .addEdge( ac )
                .addEdge( cd )
                .addEdge( df );

        expResult = graphFactory.createPathWithSource( g, a ).addEdgeAsLast( ab ).addEdgeAsLast( be ).addEdgeAsLast( ef );
        Path result = instance.route( a, f );
//        System.out.println( "expected: " + expResult );
//        System.out.println( "result: " + result );
        assertEquals( expResult, result );
//        System.out.println( "result path: " + result );
    }

    public interface RoutingAlgorithmFactory {

        public RoutingAlgorithm createRoutingAlgorithm();

        public Graph getGraph();
    }

    public static Graph createGraph() {
        DirectedNeighbourListGraphEntityFactory entityFactory = new DirectedNeighbourListGraphEntityFactory();
        DoubleDistanceFactory distanceFactory = new DoubleDistanceFactory();
        Graph graph = entityFactory.createGraph();
        Node a = entityFactory.createNode( Node.Id.generateId(), 50.1001831, 14.3856114 );
        Node b = entityFactory.createNode( Node.Id.generateId(), 50.1002725, 14.3872906 );
        Node c = entityFactory.createNode( Node.Id.generateId(), 50.1018347, 14.3857995 );
        Node d = entityFactory.createNode( Node.Id.generateId(), 50.1017039, 14.3871028 );
        Node e = entityFactory.createNode( Node.Id.generateId(), 50.1002828, 14.3878056 );
        Node f = entityFactory.createNode( Node.Id.generateId(), 50.1016489, 14.3876339 );
        Edge ab = createEdge( entityFactory, distanceFactory, a, b, false );
        Edge ac = createEdge( entityFactory, distanceFactory, a, c, false );
        Edge db = createEdge( entityFactory, distanceFactory, d, b, true );
        Edge cd = createEdge( entityFactory, distanceFactory, c, d, false );
        Edge be = createEdge( entityFactory, distanceFactory, b, e, false );
        Edge df = createEdge( entityFactory, distanceFactory, d, f, false );
        Edge ef = createEdge( entityFactory, distanceFactory, e, f, true );
        graph.addNode( a ).addNode( b ).addNode( c ).addNode( d ).addNode( e ).addNode( f );
        graph.addEdge( a, b, ab )
                .addEdge( a, c, ac )
                .addEdge( d, b, db )
                .addEdge( c, d, cd )
                .addEdge( b, e, be )
                .addEdge( d, f, df )
                .addEdge( e, f, ef );

        return graph;
    }

    private static Edge createEdge( GraphEntityFactory entityFactory, DistanceFactory distanceFactory, Node sourceNode, Node targetNode, boolean oneWay ) {
        EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder( 50 ).setOneWay( oneWay ).setLength( CoordinateUtils.calculateDistance( sourceNode.getCoordinates(), targetNode.getCoordinates() ) ).build();
        return entityFactory.createEdge( Edge.Id.generateId(), sourceNode, targetNode, distanceFactory.createFromEdgeAttributes( edgeAttributes ) )
                .setAttributes( edgeAttributes );
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> instancesToTest() {
        final Graph graph = createGraph();

        return Arrays.asList(new Object[]{
            new RoutingAlgorithmFactory() {
                @Override
                public RoutingAlgorithm createRoutingAlgorithm() {
                    return new DijkstraRoutingAlgorithm(
                            graph,
                            new DirectedNeighbourListGraphEntityFactory(),
                            new TrivialNodeDataStructure(), () -> new NodeEvaluator() {
                        @Override
                        public Distance evaluate( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode ) {
                            return sourceNode.getDistance().add( edgeFromSourceToTarget.getDistance() );
                        }
                    },
                            new DoubleDistanceFactory()
                    );
                }

                @Override
                public Graph getGraph() {
                    return graph;
                }

            } },
                // pointlessly second test
                new Object[]{
                    new RoutingAlgorithmFactory() {
                @Override
                public RoutingAlgorithm createRoutingAlgorithm() {
                    return new DijkstraRoutingAlgorithm(
                            graph,
                            new DirectedNeighbourListGraphEntityFactory(),
                            new TrivialNodeDataStructure(), () -> new NodeEvaluator() {
                        @Override
                        public Distance evaluate( Node sourceNode, Edge edgeFromSourceToTarget, Node targetNode ) {
                            return sourceNode.getDistance().add( edgeFromSourceToTarget.getDistance() );
                        }
                    },
                            new DoubleDistanceFactory()
                    );
                }

                @Override
                public Graph getGraph() {
                    return graph;
                }

            } }
        );
    }

}
