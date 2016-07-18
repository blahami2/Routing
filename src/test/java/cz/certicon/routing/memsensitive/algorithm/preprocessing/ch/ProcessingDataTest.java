/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleGraphBuilder;
import cz.certicon.routing.model.entity.GraphBuilder;
import gnu.trove.iterator.TIntIterator;
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
public class ProcessingDataTest {

    private Graph graph;

    public ProcessingDataTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        GraphBuilder<Graph> gb = new SimpleGraphBuilder( 5, 4, DistanceType.LENGTH );
        for ( int i = 0; i < 5; i++ ) {
            gb.addNode( i, i, i, i, i );
        }
        gb.addEdge( 1, 1, 1, 4, 1, 1, 1, true );
        gb.addEdge( 2, 1, 1, 1, 2, 1, 1, true );
        gb.addEdge( 3, 1, 1, 2, 3, 1, 1, true );
        gb.addEdge( 4, 1, 1, 3, 5, 1, 1, true );
        graph = gb.build();
        int[][][] trs = { {}, { { 1, 2 } }, {}, {}, {} };
        graph.setTurnRestrictions( trs );
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addShortcut method, of class ProcessingData.
     */
    @Test
    public void testAddShortcut() {
        System.out.println( "addShortcut" );
        ProcessingData data = new ProcessingData( graph );
        data.addShortcut( 0, 1 );
        data.addShortcut( 2, 3 );
        // print everything
        System.out.println( data.edgeTrs );
        System.out.println( data.shortcutsTrs );
        System.out.println( data.tmpShortcutsTrs );
        System.out.println( data.turnRestrictions );
        assertEquals( "{2=[{1, 0}],1=[{1, 0}]}", data.edgeTrs.toString() );
        assertEquals( "{5=[{1, 1}, {1, 2}],4=[{1, 0}, {1, 2}],2=[{1, 0}],1=[{1, 1}]}", data.shortcutsTrs.toString() );
        assertEquals( "{}", data.tmpShortcutsTrs.toString() );
        assertEquals( "{1=[{4, 2}, {1, 5}, {4, 5}]}", data.turnRestrictions.toString() );
        // custom toString might be useful - sort output => not limited to implementation details
    }

    /**
     * Test of addTemporaryShortcut method, of class ProcessingData.
     */
    @Test
    public void testAddTemporaryShortcut() {
        System.out.println( "addTemporaryShortcut" );
//        int startEdge = 0;
//        int endEdge = 0;
//        ProcessingData instance = null;
//        instance.addTemporaryShortcut( startEdge, endEdge );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of clearTemporaryShortcuts method, of class ProcessingData.
     */
    @Test
    public void testClearTemporaryShortcuts() {
        System.out.println( "clearTemporaryShortcuts" );
//        ProcessingData instance = null;
//        instance.clearTemporaryShortcuts();
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of size method, of class ProcessingData.
     */
    @Test
    public void testSize() {
        System.out.println( "size" );
//        ProcessingData instance = null;
//        int expResult = 0;
//        int result = instance.size();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getIncomingEdgesIterator method, of class ProcessingData.
     */
    @Test
    public void testGetIncomingEdgesIterator() {
        System.out.println( "getIncomingEdgesIterator" );
//        int node = 0;
//        ProcessingData instance = null;
//        TIntIterator expResult = null;
//        TIntIterator result = instance.getIncomingEdgesIterator( node );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getOutgoingEdgesIterator method, of class ProcessingData.
     */
    @Test
    public void testGetOutgoingEdgesIterator() {
        System.out.println( "getOutgoingEdgesIterator" );
//        int node = 0;
//        ProcessingData instance = null;
//        TIntIterator expResult = null;
//        TIntIterator result = instance.getOutgoingEdgesIterator( node );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getEdgeOrigId method, of class ProcessingData.
     */
    @Test
    public void testGetEdgeOrigId() {
        System.out.println( "getEdgeOrigId" );
//        int edge = 0;
//        long startId = 0L;
//        ProcessingData instance = null;
//        long expResult = 0L;
//        long result = instance.getEdgeOrigId( edge, startId );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getOtherNode method, of class ProcessingData.
     */
    @Test
    public void testGetOtherNode() {
        System.out.println( "getOtherNode" );
//        int edge = 0;
//        int node = 0;
//        ProcessingData instance = null;
//        int expResult = 0;
//        int result = instance.getOtherNode( edge, node );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getSource method, of class ProcessingData.
     */
    @Test
    public void testGetSource() {
        System.out.println( "getSource" );
//        int edge = 0;
//        ProcessingData instance = null;
//        int expResult = 0;
//        int result = instance.getSource( edge );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getTarget method, of class ProcessingData.
     */
    @Test
    public void testGetTarget() {
        System.out.println( "getTarget" );
//        int edge = 0;
//        ProcessingData instance = null;
//        int expResult = 0;
//        int result = instance.getTarget( edge );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of getLength method, of class ProcessingData.
     */
    @Test
    public void testGetLength() {
        System.out.println( "getLength" );
//        int edge = 0;
//        ProcessingData instance = null;
//        float expResult = 0.0F;
//        float result = instance.getLength( edge );
//        assertEquals( expResult, result, 0.0 );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

    /**
     * Test of isValidWay method, of class ProcessingData.
     */
    @Test
    public void testIsValidWay() {
        System.out.println( "isValidWay" );
//        NodeState state = null;
//        int targetEdge = 0;
//        Map<NodeState, NodeState> predecessorArray = null;
//        ProcessingData instance = null;
//        boolean expResult = false;
//        boolean result = instance.isValidWay( state, targetEdge, predecessorArray );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
    }

}
