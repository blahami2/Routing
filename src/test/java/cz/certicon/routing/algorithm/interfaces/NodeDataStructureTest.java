/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.algorithm.interfaces;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.EdgeData;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.data.number.LengthDistanceFactory;
import cz.certicon.routing.application.algorithm.datastructures.TrivialNodeDataStructure;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighborListGraphEntityFactory;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import cz.certicon.routing.model.entity.common.SimpleEdgeAttributes;
import cz.certicon.routing.model.entity.common.SimpleEdgeData;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
@RunWith( Parameterized.class )
public class NodeDataStructureTest {

    private final NodeDataStructureImplFactory factory;
    private final DirectedNeighborListGraphEntityFactory graphFactory;
    private final LengthDistanceFactory distanceFactory;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public NodeDataStructureTest( NodeDataStructureImplFactory factory ) {
        EdgeAttributes edgeAttributes = SimpleEdgeAttributes.builder().build();
        this.factory = factory;
        this.graphFactory = new DirectedNeighborListGraphEntityFactory();
        this.distanceFactory = new LengthDistanceFactory();
        int size = 20;
        for ( int i = 1; i < size; i++ ) {
            for ( int j = 1; j < size; j++ ) {
                Node n = graphFactory.createNode( Node.Id.generateId(), i, j );
                EdgeData edgeData = new SimpleEdgeData( 0, true, i * j );
                n.setDistance( distanceFactory.createFromEdgeData( edgeData ) );
                nodes.add( n );
            }
        }
        Collections.shuffle( nodes );
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
     * Test of extractMin method, of class NodeDataStructure.
     */
    @Test
    public void testExtractMin() {
        System.out.println( "extractMin" );
        NodeDataStructure<Node> instance = factory.createNodeDataStructure();
        for ( Node node : nodes ) {
            instance.add( node, node.getDistance().getEvaluableValue() );
        }
        Node min = nodes.get( 0 );
        for ( Node node : nodes ) {
            if ( min.getDistance().isGreaterThan( node.getDistance() ) ) {
                min = node;
            }
        }
        Node expResult = min;
        Node result = instance.extractMin();
        assertEquals( expResult, result );
    }

    /**
     * Test of add method, of class NodeDataStructure.
     */
    @Test
    public void testAdd() {
        System.out.println( "add" );
        Node node = nodes.get( 0 );
        NodeDataStructure<Node> instance = factory.createNodeDataStructure();
        instance.add( node, node.getDistance().getEvaluableValue() );
        Node expResult = node;
        Node result = instance.extractMin();
        assertEquals( expResult, result );
    }

    /**
     * Test of remove method, of class NodeDataStructure.
     */
    @Test
    public void testRemove() {
        System.out.println( "remove" );
        Node node = nodes.get( 0 );
        Node max = nodes.get( nodes.size() - 1 );
        NodeDataStructure<Node> instance = factory.createNodeDataStructure();
        instance.add( node, node.getDistance().getEvaluableValue() );
        instance.add( max, max.getDistance().getEvaluableValue() );
        Node expResult = max;
        instance.remove( node );
        Node result = instance.extractMin();
        assertEquals( expResult, result );
    }

    /**
     * Test of notifyDataChange method, of class NodeDataStructure.
     */
    @Test
    public void testNotifyDataChange() {
        System.out.println( "notifyDataChange" );
        assertTrue( true );
    }

    /**
     * Test of clear method, of class NodeDataStructure.
     */
    @Test
    public void testClear() {
        System.out.println( "clear" );
        NodeDataStructure<Node> instance = factory.createNodeDataStructure();
        for ( Node node : nodes ) {
            instance.add( node, node.getDistance().getEvaluableValue() );
        }
        instance.clear();
        assertTrue( instance.isEmpty() );
    }

    /**
     * Test of isEmpty method, of class NodeDataStructure.
     */
    @Test
    public void testIsEmpty() {
        System.out.println( "isEmpty" );
        NodeDataStructure<Node> instance = factory.createNodeDataStructure();
        assertEquals( true, instance.isEmpty() );
        instance.add( nodes.get( 0 ), nodes.get( 0 ).getDistance().getEvaluableValue() );
        assertEquals( false, instance.isEmpty() );
    }

    public interface NodeDataStructureImplFactory {

        public NodeDataStructure createNodeDataStructure();
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> instancesToTest() {
        return Arrays.asList(
                new Object[]{
                    new NodeDataStructureImplFactory() {
                @Override
                public NodeDataStructure createNodeDataStructure() {
                    return new TrivialNodeDataStructure();
                }
            }
                },
                // pointlessly second test
                new Object[]{
                    new NodeDataStructureImplFactory() {
                @Override
                public NodeDataStructure createNodeDataStructure() {
                    return new TrivialNodeDataStructure();
                }
            }
                }
        );
    }

}
