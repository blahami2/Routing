/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithms;

import static cz.certicon.routing.GlobalOptions.MEASURE_STATS;
import static cz.certicon.routing.GlobalOptions.MEASURE_TIME;
import cz.certicon.routing.application.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.application.*;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet.NodeEntry;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import java.util.Map;
import static cz.certicon.routing.application.RoutingAlgorithm.Utils.*;

/**
 * Basic routing algorithm implementation using the optimal Dijkstra.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private final Graph graph;
    private final int[] nodePredecessorArray;
    private final float[] nodeDistanceArray;
    private final BitArray nodeClosedArray;
    private final NodeDataStructure<Integer> nodeDataStructure;

    public DijkstraRoutingAlgorithm( Graph graph ) {
        this.graph = graph;
        this.nodePredecessorArray = new int[graph.getNodeCount()];
        this.nodeDistanceArray = new float[graph.getNodeCount()];
        this.nodeClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
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
        // clear the data
        graph.resetNodeClosedArray( nodeClosedArray );
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
        nodeDataStructure.clear();

        // set the source points (add to the queue)
        initArrays( graph, nodeDistanceArray, nodeDataStructure, from );
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            int node = nodeDataStructure.extractMin();
            if ( MEASURE_STATS ) {
                StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
            }
            float distance = nodeDistanceArray[node];
            nodeClosedArray.set( node, true );
//            System.out.println( "Extracted: " + node + " with distance: " + distance );
            if ( finalDistance < distance ) {
                break;
            }
            if ( to.containsKey( node ) ) {
//                System.out.println( "found end node: " + node );
                double nodeDistance = distance + getDistance( graph, to.get( node ) );
                if ( nodeDistance < finalDistance ) {
//                    System.out.println( nodeDistance + " < " + finalDistance );
                    finalNode = node;
                    finalDistance = nodeDistance;
                }
            }
//            System.out.println( "outgoing array: " + Arrays.toString( graph.getOutgoingEdges( node ) ) );
            // foreach neighbour T of node S
            TIntIterator it = graph.getOutgoingEdgesIterator( node );
            while ( it.hasNext() ) {
                int edge = it.next();
                int target = graph.getOtherNode( edge, node );
//                System.out.println( "edge = " + edge + ", target = " + target );
                if ( !nodeClosedArray.get( target ) ) {
                    if ( MEASURE_STATS ) {
                        StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                    }
                    // calculate it's distance S + path from S to T
                    float targetDistance = nodeDistanceArray[target];
                    float alternativeDistance = distance + graph.getLength( edge );
                    // replace if lower than actual
                    if ( alternativeDistance < targetDistance ) {
                        nodeDistanceArray[target] = alternativeDistance;
                        nodePredecessorArray[target] = edge;
                        nodeDataStructure.notifyDataChange( target, alternativeDistance );
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
        // if the final node has been found, build route from predecessors and return
        if ( finalNode != -1 ) {
//            System.out.println( "orig node as target: " + graph.getNodeOrigId( finalNode ) );
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalNode ) );
            int pred = nodePredecessorArray[finalNode];
            int currentNode = finalNode;
            while ( graph.isValidPredecessor( pred ) ) {
//                System.out.println( "predecessor: " + pred + ", source = " + graph.getNodeOrigId( graph.getSource( pred ) ) + ", target = " + graph.getNodeOrigId( graph.getTarget( pred ) ) );
                routeBuilder.addEdgeAsFirst( graph, graph.getEdgeOrigId( pred ) );
                int node = graph.getOtherNode( pred, currentNode );
//                System.out.println( "node = " + graph.getNodeOrigId( node ) );
                pred = nodePredecessorArray[node];
                currentNode = node;
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
