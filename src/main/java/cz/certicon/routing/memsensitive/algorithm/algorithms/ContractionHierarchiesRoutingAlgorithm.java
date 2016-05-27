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
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private final Graph graph;
    private final int[] nodeFromPredecessorArray;
    private final float[] nodeFromDistanceArray;
    private final BitArray nodeFromClosedArray;
    private final NodeDataStructure<Integer> nodeFromDataStructure;
    private final int[] nodeToPredecessorArray;
    private final float[] nodeToDistanceArray;
    private final BitArray nodeToClosedArray;
    private final NodeDataStructure<Integer> nodeToDataStructure;
    private final PreprocessedData preprocessedData;

    public ContractionHierarchiesRoutingAlgorithm( Graph graph, PreprocessedData preprocessedData ) {
        this.graph = graph;
        this.nodeFromPredecessorArray = new int[graph.getNodeCount()];
        this.nodeFromDistanceArray = new float[graph.getNodeCount()];
        this.nodeFromClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeFromDataStructure = new JgraphtFibonacciDataStructure();
        this.nodeToPredecessorArray = new int[graph.getNodeCount()];
        this.nodeToDistanceArray = new float[graph.getNodeCount()];
        this.nodeToClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeToDataStructure = new JgraphtFibonacciDataStructure();
        this.preprocessedData = preprocessedData;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, Float> from, Map<Integer, Float> to ) {
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        graph.resetNodeDistanceArray( nodeFromDistanceArray );
        graph.resetNodePredecessorArray( nodeFromPredecessorArray );
        graph.resetNodeClosedArray( nodeFromClosedArray );
        nodeFromDataStructure.clear();
        graph.resetNodeDistanceArray( nodeToDistanceArray );
        graph.resetNodePredecessorArray( nodeToPredecessorArray );
        graph.resetNodeClosedArray( nodeToClosedArray );
        nodeToDataStructure.clear();

        for ( Map.Entry<Integer, Float> entry : from.entrySet() ) {
            int node = entry.getKey();
            float distance = entry.getValue();
            nodeFromDistanceArray[node] = distance;
            nodeFromDataStructure.add( node, distance );
        }
        for ( Map.Entry<Integer, Float> entry : to.entrySet() ) {
            int node = entry.getKey();
            float distance = entry.getValue();
            nodeToDistanceArray[node] = distance;
            nodeToDataStructure.add( node, distance );
        }
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeFromDataStructure.isEmpty() || !nodeToDataStructure.isEmpty() ) {
            if ( !nodeFromDataStructure.isEmpty() ) {
                int currentNode = nodeFromDataStructure.extractMin();
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeFromClosedArray.set( currentNode, true );
                int sourceRank = preprocessedData.getRank( currentNode );
                float currentDistance = nodeFromDistanceArray[currentNode];

                if ( finalDistance < currentDistance ) {
                    // end this part, everything else can only be worse
                    nodeFromDataStructure.clear();
                } else {
                    if ( nodeToClosedArray.get( currentNode ) ) {
                        float nodeDistance = currentDistance + nodeToDistanceArray[currentNode];
                        if ( nodeDistance < finalDistance ) {
                            finalDistance = nodeDistance;
                            finalNode = currentNode;
                        }
                    }

                    for ( int i = 0; i < graph.getOutgoingEdges( currentNode ).length; i++ ) {
                        int edge = graph.getOutgoingEdges( currentNode )[i];
                        int otherNode = graph.getOtherNode( edge, currentNode );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            float otherNodeDistance = nodeFromDistanceArray[otherNode];
                            float distance = currentDistance + graph.getLength( edge );
                            if ( distance < otherNodeDistance ) {
                                nodeFromDistanceArray[otherNode] = distance;
                                nodeFromPredecessorArray[otherNode] = edge;
                                nodeFromDataStructure.notifyDataChange( otherNode, distance );
                            }
                        }
                    }
                    for ( int i = 0; i < preprocessedData.getOutgoingShortcuts( currentNode ).length; i++ ) {
                        int shortcut = preprocessedData.getOutgoingShortcuts( currentNode )[i];
                        int otherNode = preprocessedData.getTarget( shortcut );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            float otherNodeDistance = nodeFromDistanceArray[otherNode];
                            float distance = currentDistance + preprocessedData.getLength( shortcut, graph );
                            if ( distance < otherNodeDistance ) {
                                nodeFromDistanceArray[otherNode] = distance;
                                nodeFromPredecessorArray[otherNode] = shortcut + graph.getEdgeCount();
                                nodeFromDataStructure.notifyDataChange( otherNode, distance );
                            }
                        }
                    }
                }
            }
            if ( !nodeToDataStructure.isEmpty() ) {
                int currentNode = nodeToDataStructure.extractMin();
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                nodeToClosedArray.set( currentNode, true );
                int sourceRank = preprocessedData.getRank( currentNode );
                float currentDistance = nodeFromDistanceArray[currentNode];

                if ( finalDistance < currentDistance ) {
                    // end this part, everything else can only be worse
                    nodeFromDataStructure.clear();
                } else {
                    if ( nodeFromClosedArray.get( currentNode ) ) {
                        float nodeDistance = currentDistance + nodeFromDistanceArray[currentNode];
                        if ( nodeDistance < finalDistance ) {
                            finalDistance = nodeDistance;
                            finalNode = currentNode;
                        }
                    }
                    for ( int i = 0; i < graph.getIncomingEdges( currentNode ).length; i++ ) {
                        int edge = graph.getIncomingEdges( currentNode )[i];
                        int otherNode = graph.getOtherNode( edge, currentNode );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            float otherNodeDistance = nodeToDistanceArray[otherNode];
                            float distance = currentDistance + graph.getLength( edge );
                            if ( distance < otherNodeDistance ) {
                                nodeToDistanceArray[otherNode] = distance;
                                nodeToPredecessorArray[otherNode] = edge;
                                nodeToDataStructure.notifyDataChange( otherNode, distance );
                            }
                        }
                    }
                    for ( int i = 0; i < preprocessedData.getIncomingShortcuts( currentNode ).length; i++ ) {
                        int shortcut = preprocessedData.getIncomingShortcuts( currentNode )[i];
                        int otherNode = preprocessedData.getSource( shortcut );
                        if ( preprocessedData.getRank( otherNode ) > sourceRank ) {
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            float otherNodeDistance = nodeToDistanceArray[otherNode];
                            float distance = currentDistance + preprocessedData.getLength( shortcut, graph );
                            if ( distance < otherNodeDistance ) {
                                nodeToDistanceArray[otherNode] = distance;
                                nodeToPredecessorArray[otherNode] = shortcut + graph.getEdgeCount();
                                nodeToDataStructure.notifyDataChange( otherNode, distance );
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
        if ( finalNode != -1 ) {
            // set target to final, then add as first, then add as last for the "to" dijkstra
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalNode ) );
            int pred = nodeFromPredecessorArray[finalNode];
            int currentNode = finalNode;
            while ( graph.isValidPredecessor( pred ) ) {
                int node = addEdgeAsFirst( routeBuilder, pred, currentNode );
                pred = nodeFromPredecessorArray[node];
                currentNode = node;
            }
            currentNode = finalNode;
            pred = nodeToPredecessorArray[finalNode];
            while ( graph.isValidPredecessor( pred ) ) {
                int node = addEdgeAsLast( routeBuilder, pred, currentNode );
                pred = nodeToPredecessorArray[node];
                currentNode = node;
            }
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
            routeBuilder.addEdgeAsLast( graph, graph.getEdgeOrigId( edge ) );
            return graph.getOtherNode( edge, currentNode );
        } else { // shortcut
            edge -= graph.getEdgeCount();
            addEdgeAsLast( routeBuilder, preprocessedData.getEndEdge( edge ), currentNode );
            addEdgeAsLast( routeBuilder, preprocessedData.getStartEdge( edge ), currentNode );
            return preprocessedData.getSource( edge );
        }
    }
}
