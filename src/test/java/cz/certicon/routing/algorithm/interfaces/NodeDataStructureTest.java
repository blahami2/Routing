/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.algorithm.interfaces;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.data.simple.SimpleDistanceFactory;
import cz.certicon.routing.application.algorithm.datastructures.TrivialNodeDataStructure;
import cz.certicon.routing.model.entity.neighbourlist.DirectedNeighbourListGraphEntityFactory;
import cz.certicon.routing.model.entity.Edge;
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
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 *
 * @author Michael Blaha  {@literal <michael.blaha@certicon.cz>}
 */
@RunWith( Parameterized.class )
public class NodeDataStructureTest {

    private final NodeDataStructureImplFactory factory;
    private final DirectedNeighbourListGraphEntityFactory graphFactory;
    private final SimpleDistanceFactory distanceFactory;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public NodeDataStructureTest( NodeDataStructureImplFactory factory ) {
        this.factory = factory;
        this.graphFactory = new DirectedNeighbourListGraphEntityFactory();
        this.distanceFactory = new SimpleDistanceFactory();
        int size = 20;
        for ( int i = 1; i < size; i++ ) {
            for ( int j = 1; j < size; j++ ) {
                Node n = graphFactory.createNode(Node.Id.generateId(),  i, j );
                n.setDistance( distanceFactory.createFromDouble(i * j ) );
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
        NodeDataStructure instance = factory.createNodeDataStructure();
        nodes.stream().forEach( ( node ) -> {
            instance.add( node );
        } );
        Node expResult = nodes.stream().min( ( Node o1, Node o2 ) -> o1.getDistance().compareTo( o2.getDistance() ) ).get();
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
        NodeDataStructure instance = factory.createNodeDataStructure();
        instance.add( node );
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
        NodeDataStructure instance = factory.createNodeDataStructure();
        instance.add( node ).add( max );
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
        NodeDataStructure instance = factory.createNodeDataStructure();
        nodes.stream().forEach( ( node ) -> {
            instance.add( node );
        } );
        instance.clear();
        assertTrue( instance.isEmpty() );
    }

    /**
     * Test of isEmpty method, of class NodeDataStructure.
     */
    @Test
    public void testIsEmpty() {
        System.out.println( "isEmpty" );
        NodeDataStructure instance = factory.createNodeDataStructure();
        assertEquals( true, instance.isEmpty() );
        instance.add( nodes.get( 0 ) );
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
