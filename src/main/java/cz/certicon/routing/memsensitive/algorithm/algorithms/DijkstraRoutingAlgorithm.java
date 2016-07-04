/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.algorithms;

import cz.certicon.routing.GlobalOptions;
import static cz.certicon.routing.GlobalOptions.DEBUG_DISPLAY;
import static cz.certicon.routing.GlobalOptions.MEASURE_STATS;
import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.common.SimpleNodeState;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.presentation.DebugViewer;
import cz.certicon.routing.memsensitive.presentation.jxmapviewer.JxDebugViewer;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private final Graph graph;

    public DijkstraRoutingAlgorithm( Graph graph ) {
        this.graph = graph;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException {
        Map<NodeState, NodeState> nodePredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeDistanceArray = new HashMap<>();
        Set<NodeState> nodeClosedArray = new HashSet<>();
        NodeDataStructure<NodeState> nodeDataStructure = new JgraphtFibonacciDataStructure();
        DebugViewer debugViewer = null;
        if ( DEBUG_DISPLAY ) {
            debugViewer = new JxDebugViewer( GlobalOptions.DEBUG_DISPLAY_PROPERTIES, GlobalOptions.DEBUG_DISPLAY_PAUSE );
        }
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        nodeDataStructure.clear();

        for ( NodeEntry nodeEntry : from.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            NodeState state = NodeState.Factory.newInstance( node, edge );
            nodeDistanceArray.put( state, distance );
            nodeDataStructure.add( state, distance );
            // DEBUG
//            System.out.println( "From: node#" + node + ", edge#" + edge + ", distance = " + distance );
        }
        NodeState finalState = null;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeDataStructure.isEmpty() ) {
            NodeState state = nodeDataStructure.extractMin();
            if ( DEBUG_DISPLAY ) {
                System.out.println( "#" + graph.getNodeOrigId( state.getNode() ) + "-closed" );
                debugViewer.closeEdge( graph.getEdgeOrigId( state.getEdge() ) );
            }
            if ( MEASURE_STATS ) {
                StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
            }
            float distance = nodeDistanceArray.get( state );
            // DEBUG
//            System.out.println( "Extracted: node#" + state.getNode() + ", edge#" + state.getEdge() + ", distance = " + distance );
            nodeClosedArray.add( state );
//            System.out.println( "Extracted: " + node + " with distance: " + distance );
            if ( finalDistance < distance ) {
                break;
            }
            if ( to.containsKey( state.getNode() ) ) { // found one of the final nodes
                if ( graph.isValidWay( state, to.get( state.getNode() ).getEdgeId(), nodePredecessorArray ) ) { // is able to turn there
                    // DEBUG
//                    System.out.println( "Final: node#" + state.getNode() + ", edge#" + state.getEdge() + ", distance = " + distance );
//                System.out.println( "found end node: " + node );
                    double nodeDistance = distance + to.get( state.getNode() ).getDistance();
                    if ( nodeDistance < finalDistance ) {
//                    System.out.println( nodeDistance + " < " + finalDistance );
                        finalState = state;
                        finalDistance = nodeDistance;
                    }
                }
            }
//            System.out.println( "outgoing array: " + Arrays.toString( graph.getOutgoingEdges( node ) ) );
            TIntIterator it = graph.getOutgoingEdgesIterator( state.getNode() );
            while ( it.hasNext() ) {
                int edge = it.next();
                int target = graph.getOtherNode( edge, state.getNode() );
                NodeState targetState = NodeState.Factory.newInstance( target, edge );
                // DEBUG
//                System.out.println( "Check: node#" + targetState.getNode() + ", edge#" + targetState.getEdge() + ", closed = " + nodeClosedArray.contains( targetState ) );
//                System.out.println( "edge = " + edge + ", target = " + target );
                if ( !nodeClosedArray.contains( targetState ) && ( state.getEdge() < 0 || target != graph.getOtherNode( state.getEdge(), state.getNode() ) ) ) { // if not closed and not returning to the previous node
                    if ( MEASURE_STATS ) {
                        StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                    }
                    if ( !graph.isValidWay( state, edge, nodePredecessorArray ) ) {
                        // DEBUG
//                        System.out.println( "Restricted: node#" + targetState.getNode() + ", edge#" + targetState.getEdge() );
                        if ( DEBUG_DISPLAY ) {
                            System.out.println( "#" + graph.getNodeOrigId( target ) + "-restricted" );
                            debugViewer.blinkEdge( graph.getEdgeOrigId( edge ) );
                        }
                    } else {
                        if ( DEBUG_DISPLAY ) {
                            System.out.println( "#" + graph.getNodeOrigId( target ) + "-visited" );
                            debugViewer.displayEdge( graph.getEdgeOrigId( edge ) );
                        }
                        float targetDistance = ( nodeDistanceArray.containsKey( targetState ) ) ? nodeDistanceArray.get( targetState ) : Float.MAX_VALUE;
                        float alternativeDistance = distance + graph.getLength( edge );
                        // DEBUG
//                        System.out.println( "Visited: node#" + targetState.getNode() + ", edge#" + targetState.getEdge() + ", distance current = " + targetDistance + ", distance alternative = " + alternativeDistance );
                        if ( alternativeDistance < targetDistance ) {
                            nodeDistanceArray.put( targetState, alternativeDistance );
                            nodePredecessorArray.put( targetState, state );
                            nodeDataStructure.notifyDataChange( targetState, alternativeDistance );
                        }
                    }
                }
            }
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.STOP );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.START );
        }
        if ( DEBUG_DISPLAY ) {
            debugViewer.close();
        }
        if ( finalState != null ) {
//            System.out.println( "orig node as target: " + graph.getNodeOrigId( finalNode ) );
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalState.getNode() ) );
            NodeState currentState = finalState;
            while ( nodePredecessorArray.containsKey( currentState ) && graph.isValidPredecessor( currentState.getEdge() ) ) {// omit the first edge // starting from crossroad
//                System.out.println( "predecessor: " + pred + ", source = " + graph.getNodeOrigId( graph.getSource( pred ) ) + ", target = " + graph.getNodeOrigId( graph.getTarget( pred ) ) );
                routeBuilder.addEdgeAsFirst( graph, graph.getEdgeOrigId( currentState.getEdge() ) );
                currentState = nodePredecessorArray.get( currentState );
            }
        } else {
            throw new RouteNotFoundException();
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }
        return routeBuilder.build();
    }

}
