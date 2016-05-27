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
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
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
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, Float> from, Map<Integer, Float> to ) {
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
        graph.resetNodeClosedArray( nodeClosedArray );
        nodeDataStructure.clear();

        for ( Map.Entry<Integer, Float> entry : from.entrySet() ) {
            int node = entry.getKey();
            float distance = entry.getValue();
            nodeDistanceArray[node] = distance;
            nodeDataStructure.add( node, distance );
//            System.out.println( "adding: " + node + " with distance: " + distance );
        }
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeDataStructure.isEmpty() ) {
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
                double nodeDistance = distance + to.get( node );
                if ( nodeDistance < finalDistance ) {
                    System.out.println( nodeDistance + " < " + finalDistance );
                    finalNode = node;
                    finalDistance = nodeDistance;
                }
            }
//            System.out.println( "outgoing array: " + Arrays.toString( graph.getOutgoingEdges( node ) ) );
            Iterator<Integer> it = graph.getOutgoingEdgesIterator( node );
            while ( it.hasNext() ) {
                int edge = it.next();
                int target = graph.getOtherNode( edge, node );
//                System.out.println( "edge = " + edge + ", target = " + target );
                if ( !nodeClosedArray.get( target ) ) {
                    if ( MEASURE_STATS ) {
                        StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                    }
                    float targetDistance = nodeDistanceArray[target];
                    float alternativeDistance = distance + graph.getLength( edge );
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
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }
        return routeBuilder.build();
    }

}
