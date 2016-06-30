/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.algorithms;

import static cz.certicon.routing.GlobalOptions.MEASURE_STATS;
import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesUbRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private static final double ARRAY_COPY_RATIO = 0.01;

    private final Graph graph;
    private final PreprocessedData preprocessedData;

    public ContractionHierarchiesUbRoutingAlgorithm( Graph graph, PreprocessedData preprocessedData ) {
        this.graph = graph;
        this.preprocessedData = preprocessedData;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException {
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        Map<NodeState, NodeState> nodeFromPredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeFromDistanceArray = new HashMap<>();
        Set<NodeState> nodeFromClosedArray = new HashSet<>();
        NodeDataStructure<NodeState> nodeFromDataStructure = new JgraphtFibonacciDataStructure();
        TIntObjectMap<List<Pair<NodeState, Float>>> nodeFromStates = new TIntObjectHashMap<>();
        Map<NodeState, NodeState> nodeToPredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeToDistanceArray = new HashMap<>();
        Set<NodeState> nodeToClosedArray = new HashSet<>();
        NodeDataStructure<NodeState> nodeToDataStructure = new JgraphtFibonacciDataStructure();
        TIntObjectMap<List<Pair<NodeState, Float>>> nodeToStates = new TIntObjectHashMap<>();

        for ( NodeEntry nodeEntry : from.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            NodeState state = NodeState.Factory.newInstance( node, edge );
            nodeFromDistanceArray.put( state, distance );
            nodeFromDataStructure.add( state, distance );
        }
        for ( NodeEntry nodeEntry : to.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            NodeState state = NodeState.Factory.newInstance( node, edge );
            nodeToDistanceArray.put( state, distance );
            nodeToDataStructure.add( state, distance );
        }
//        NodeState finalState = null;
        NodeState finalFromState = null;
        NodeState finalToState = null;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeFromDataStructure.isEmpty() || !nodeToDataStructure.isEmpty() ) {
            if ( !nodeFromDataStructure.isEmpty() ) {
                NodeState state = nodeFromDataStructure.extractMin();
//                System.out.println( "F: state = " + state );
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeFromClosedArray.add( state );
                int sourceRank = preprocessedData.getRank( state.getNode() );
                float currentDistance = nodeFromDistanceArray.get( state );
                List<Pair<NodeState, Float>> get = nodeFromStates.get( state.getNode() );
                if ( get == null ) {
                    get = new ArrayList<>();
                    nodeFromStates.put( state.getNode(), get );
                }
                get.add( new Pair<>( state, currentDistance ) );
                if ( finalDistance < currentDistance ) {
                    // end this part, everything else can only be worse
                    nodeFromDataStructure.clear();
                } else {
                    if ( nodeToStates.containsKey( state.getNode() ) ) {
//                        System.out.println( "F-upper bound: " + state + ", distance = " + currentDistance );
                        List<Pair<NodeState, Float>> toStates = nodeToStates.get( state.getNode() );
                        for ( Pair<NodeState, Float> toState : toStates ) {
                            float nodeDistance = currentDistance + toState.b;
                            if ( nodeDistance < finalDistance ) {
                                finalDistance = nodeDistance;
                                finalFromState = state;
                                finalToState = toState.a;
                            }
                            break;
                        }
                    }
                    TIntIterator outgoingEdgesIterator = preprocessedData.getOutgoingEdgesIterator( state.getNode(), graph );
//                    System.out.println( "F-iterating over neighbours" );
                    while ( outgoingEdgesIterator.hasNext() ) {
                        int edge = outgoingEdgesIterator.next();
//                        System.out.println( "F-edge#" + edge );
                        int otherNode = preprocessedData.getOtherNode( edge, state.getNode(), graph );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
//                            System.out.println( "F-rank-ok-node#" + otherNode );
                            if ( !preprocessedData.isValidWay( state, edge, nodeFromPredecessorArray, graph ) ) {
                                continue;
                            }
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            NodeState targetState = NodeState.Factory.newInstance( otherNode, edge );
                            float targetDistance = ( nodeFromDistanceArray.containsKey( targetState ) ) ? nodeFromDistanceArray.get( targetState ) : Float.MAX_VALUE;
                            float distance = currentDistance + preprocessedData.getLength( edge, graph );
                            if ( distance < targetDistance ) {
                                nodeFromDistanceArray.put( targetState, distance );
                                nodeFromPredecessorArray.put( targetState, state );
                                nodeFromDataStructure.notifyDataChange( targetState, distance );
                            }
                        } else {
//                            System.out.println( "F-rank-not-ok-node#" + state.getNode() + " -> " + sourceRank + ", node#" + otherNode + " -> " + preprocessedData.getRank( otherNode ) );
                        }
                    }
                }
            }
            if ( !nodeToDataStructure.isEmpty() ) {
                NodeState state = nodeToDataStructure.extractMin();
//                System.out.println( "T: state = " + state );
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeToClosedArray.add( state );
                int sourceRank = preprocessedData.getRank( state.getNode() );
                float currentDistance = nodeToDistanceArray.get( state );
                List<Pair<NodeState, Float>> get = nodeToStates.get( state.getNode() );
                if ( get == null ) {
                    get = new ArrayList<>();
                    nodeToStates.put( state.getNode(), get );
                }
                get.add( new Pair<>( state, currentDistance ) );
                if ( finalDistance < currentDistance ) {
                    // end this part, everything else can only be worse
                    nodeToDataStructure.clear();
                } else {
                    if ( nodeFromStates.containsKey( state.getNode() ) ) {
//                        System.out.println( "T-upper bound: " + state + ", distance = " + currentDistance );
                        List<Pair<NodeState, Float>> fromStates = nodeFromStates.get( state.getNode() );
                        for ( Pair<NodeState, Float> fromState : fromStates ) {
                            float nodeDistance = currentDistance + fromState.b;
                            if ( nodeDistance < finalDistance ) {
                                finalDistance = nodeDistance;
                                finalToState = state;
                                finalFromState = fromState.a;
                            }
                            break;
                        }
                    }
                    TIntIterator incomingEdgesIterator = preprocessedData.getIncomingEdgesIterator( state.getNode(), graph );
                    while ( incomingEdgesIterator.hasNext() ) {
                        int edge = incomingEdgesIterator.next();
                        int otherNode = preprocessedData.getOtherNode( edge, state.getNode(), graph );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                            if ( !preprocessedData.isValidWay( state, edge, nodeToPredecessorArray, graph ) ) {
                                continue;
                            }
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            NodeState targetState = NodeState.Factory.newInstance( otherNode, edge );
                            float targetDistance = ( nodeToDistanceArray.containsKey( targetState ) ) ? nodeToDistanceArray.get( targetState ) : Float.MAX_VALUE;
                            float distance = currentDistance + preprocessedData.getLength( edge, graph );
                            if ( distance < targetDistance ) {
                                nodeToDistanceArray.put( targetState, distance );
                                nodeToPredecessorArray.put( targetState, state );
                                nodeToDataStructure.notifyDataChange( targetState, distance );
                            }
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
        if ( finalFromState != null && finalToState != null ) {
//            System.out.println( "Final states = F: " + finalFromState + ", T: " + finalToState );
            // set target to final, then add as first, then add as last for the "to" dijkstra
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalFromState.getNode() ) );
            NodeState currentState = finalFromState;
            while ( nodeFromPredecessorArray.containsKey( currentState ) && graph.isValidPredecessor( currentState.getEdge() ) ) {
                addEdgeAsFirst( routeBuilder, currentState.getEdge(), currentState.getNode() );
                currentState = nodeFromPredecessorArray.get( currentState );
            }
            currentState = finalToState;
            while ( nodeToPredecessorArray.containsKey( currentState ) && graph.isValidPredecessor( currentState.getEdge() ) ) {
                addEdgeAsLast( routeBuilder, currentState.getEdge(), currentState.getNode() );
                currentState = nodeToPredecessorArray.get( currentState );
            }
        } else {
//            System.out.println( "Final states: NONE" );
            throw new RouteNotFoundException();
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }
        return routeBuilder.build();
    }

    private <R> int addEdgeAsFirst( RouteBuilder<R, Graph> routeBuilder, int edge, int currentNode ) {
        if ( edge < graph.getEdgeCount() ) { // edge
            routeBuilder.addEdgeAsFirst( graph, graph.getEdgeOrigId( edge ) );
            return graph.getOtherNode( edge, currentNode );
        } else { // shortcut
            edge -= graph.getEdgeCount();
            addEdgeAsFirst( routeBuilder, preprocessedData.getEndEdge( edge ), currentNode );
            addEdgeAsFirst( routeBuilder, preprocessedData.getStartEdge( edge ), currentNode );
            return preprocessedData.getSource( edge );
        }
    }

    private <R> int addEdgeAsLast( RouteBuilder<R, Graph> routeBuilder, int edge, int currentNode ) {
        if ( edge < graph.getEdgeCount() ) { // edge
//            System.out.println( "edge: " + edge );
            routeBuilder.addEdgeAsLast( graph, graph.getEdgeOrigId( edge ) );
            return graph.getOtherNode( edge, currentNode );
        } else { // shortcut
            edge -= graph.getEdgeCount();
//            System.out.println( "shortcut: " + edge );
            addEdgeAsLast( routeBuilder, preprocessedData.getStartEdge( edge ), currentNode );
            addEdgeAsLast( routeBuilder, preprocessedData.getEndEdge( edge ), currentNode );
            return preprocessedData.getTarget( edge );
        }
    }
}
