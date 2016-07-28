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
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RouteNotFoundException;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.algorithm.algorithms.ContractionHierarchiesRoutingAlgorithm.TurnTableSequenceOpposite;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.memsensitive.presentation.DebugViewer;
import cz.certicon.routing.memsensitive.presentation.jxmapviewer.JxDebugViewer;
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
import java.util.LinkedList;
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
        DebugViewer debugViewerFrom = null;
        DebugViewer debugViewerTo = null;
        if ( DEBUG_DISPLAY ) {
            debugViewerFrom = new JxDebugViewer( GlobalOptions.DEBUG_DISPLAY_PROPERTIES, GlobalOptions.DEBUG_DISPLAY_PAUSE, preprocessedData, graph );
            debugViewerFrom.setStepByInput( GlobalOptions.DEBUG_DISPLAY_MANUAL_STEP );
            debugViewerTo = new JxDebugViewer( GlobalOptions.DEBUG_DISPLAY_PROPERTIES, GlobalOptions.DEBUG_DISPLAY_PAUSE, preprocessedData, graph );
            debugViewerTo.setStepByInput( GlobalOptions.DEBUG_DISPLAY_MANUAL_STEP );
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
        TIntList nodesToVisited = new TIntArrayList();
        Map<NodeState, LinkedList<TurnTableSequenceOpposite>> toCarriedTtMap = new HashMap<>();

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
                if ( DEBUG_DISPLAY ) {
                    System.out.println( "F#" + graph.getNodeOrigId( state.getNode() ) + "-closed-via#" + ( state.getEdge() < 0 ? -1 : preprocessedData.getEdgeOrigId( state.getEdge(), graph ) ) );

                    if ( state.getEdge() >= 0 ) {
                        debugViewerFrom.closeEdge( preprocessedData.getEdgeOrigId( state.getEdge(), graph ) );
                    }
                }
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
                            if ( 0 <= nodeDistance && nodeDistance < finalDistance ) {
                                LinkedList<TurnTableSequenceOpposite> currentTurnTables = toCarriedTtMap.get( toState.a );
                                if ( currentTurnTables == null || preprocessedData.isValidWay( state, currentTurnTables, nodeFromPredecessorArray, graph ) ) {
                                    System.out.println( "final #1" );
                                    finalDistance = nodeDistance;
                                    finalFromState = state;
                                    finalToState = toState.a;
                                    break;
                                }
                            }
//                            break;
                        }
                    }
                    TIntIterator outgoingEdgesIterator = preprocessedData.getOutgoingEdgesIterator( state.getNode(), graph );
//                    System.out.println( "F-iterating over neighbours" );
                    while ( outgoingEdgesIterator.hasNext() ) {
                        int edge = outgoingEdgesIterator.next();
//                        System.out.println( "F-edge#" + edge );
                        int otherNode = preprocessedData.getOtherNode( edge, state.getNode(), graph );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank
                                && ( state.getEdge() < 0 || otherNode != preprocessedData.getOtherNode( state.getEdge(), state.getNode(), graph ) ) ) {
//                            System.out.println( "F-rank-ok-node#" + otherNode );
                            if ( !preprocessedData.isValidWay( state, edge, nodeFromPredecessorArray, graph ) ) {
                                if ( DEBUG_DISPLAY ) {
                                    System.out.println( "F#" + graph.getNodeOrigId( otherNode ) + "-restricted-via#" + ( edge < 0 ? -1 : preprocessedData.getEdgeOrigId( edge, graph ) ) );

                                    if ( edge >= 0 ) {
                                        debugViewerFrom.blinkEdge( preprocessedData.getEdgeOrigId( edge, graph ) );
                                    }
                                }
                                continue;
                            }
                            if ( DEBUG_DISPLAY ) {
                                System.out.println( "F#" + graph.getNodeOrigId( otherNode ) + "-visited-via#" + ( edge < 0 ? -1 : preprocessedData.getEdgeOrigId( edge, graph ) ) );
//                            System.out.println( edge + " to orig = " + preprocessedData.getEdgeOrigId( edge, graph ) );
                                if ( edge >= 0 ) {
                                    debugViewerFrom.displayEdge( preprocessedData.getEdgeOrigId( edge, graph ) );
                                }
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
                if ( DEBUG_DISPLAY ) {
                    System.out.println( "T#" + graph.getNodeOrigId( state.getNode() ) + "-closed-via#" + ( state.getEdge() < 0 ? -1 : preprocessedData.getEdgeOrigId( state.getEdge(), graph ) ) );

                    if ( state.getEdge() >= 0 ) {
                        debugViewerTo.closeEdge( preprocessedData.getEdgeOrigId( state.getEdge(), graph ) );
                    }
                }
//                System.out.println( "T: state = " + state );
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeToClosedArray.add( state );
                nodesToVisited.add( state.getNode() );
                int sourceRank = preprocessedData.getRank( state.getNode() );
                float currentDistance = nodeToDistanceArray.get( state );
                List<Pair<NodeState, Float>> get = nodeToStates.get( state.getNode() );
                if ( get == null ) {
                    get = new ArrayList<>();
                    nodeToStates.put( state.getNode(), get );
                }
                get.add( new Pair<>( state, currentDistance ) );
                LinkedList<TurnTableSequenceOpposite> currentTurnTables = toCarriedTtMap.get( state );
                if ( currentTurnTables == null ) {
                    currentTurnTables = new LinkedList<>();
                    toCarriedTtMap.put( state, currentTurnTables );
                }

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
                                if ( preprocessedData.isValidWay( fromState.a, currentTurnTables, nodeFromPredecessorArray, graph ) ) {
                                    finalDistance = nodeDistance;
                                    finalToState = state;
                                    finalFromState = fromState.a;
                                    break;
                                }
                            }
                        }
                    }
                    TIntIterator incomingEdgesIterator = preprocessedData.getIncomingEdgesIterator( state.getNode(), graph );
                    while ( incomingEdgesIterator.hasNext() ) {
                        int edge = incomingEdgesIterator.next();
                        int otherNode = preprocessedData.getOtherNode( edge, state.getNode(), graph );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank
                                && ( state.getEdge() < 0 || otherNode != preprocessedData.getOtherNode( state.getEdge(), state.getNode(), graph ) ) ) {
                            if ( !preprocessedData.isValidWay( currentTurnTables, edge ) ) {
                                if ( DEBUG_DISPLAY ) {
                                    System.out.println( "T#" + graph.getNodeOrigId( otherNode ) + "-restricted-via#" + ( edge < 0 ? -1 : preprocessedData.getEdgeOrigId( edge, graph ) ) );
                                    if ( edge >= 0 ) {
                                        debugViewerTo.blinkEdge( preprocessedData.getEdgeOrigId( edge, graph ) );
                                    }
                                }
                                continue;
                            }
                            if ( DEBUG_DISPLAY ) {
                                System.out.println( "T#" + graph.getNodeOrigId( otherNode ) + "-visited-via#" + ( edge < 0 ? -1 : preprocessedData.getEdgeOrigId( edge, graph ) ) );
                                if ( edge >= 0 ) {
                                    debugViewerTo.displayEdge( preprocessedData.getEdgeOrigId( edge, graph ) );
                                }
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
                                LinkedList<TurnTableSequenceOpposite> nextTurnTables = new LinkedList<>();
                                for ( TurnTableSequenceOpposite currentTurnTable : currentTurnTables ) {
                                    if ( currentTurnTable.edges[currentTurnTable.current - 1] == edge ) { // if still matches for the next step
                                        TurnTableSequenceOpposite next = currentTurnTable.next();
                                        nextTurnTables.add( next );
                                    }
                                }
                                if ( currentTurnTables.size() > 0 ) { // if not empty
                                    toCarriedTtMap.put( targetState, nextTurnTables );
                                }
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
            if ( DEBUG_DISPLAY ) {
                debugViewerFrom.displayNode( graph.getNodeOrigId( finalFromState.getNode() ) );
                debugViewerTo.displayNode( graph.getNodeOrigId( finalToState.getNode() ) );
            }
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
        if ( DEBUG_DISPLAY ) {
            debugViewerFrom.close();
            debugViewerTo.close();
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
