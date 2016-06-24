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
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.EffectiveUtils;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AstarRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private final Graph graph;
    private final DistanceType distanceType;
    private final int[] nodePredecessorArray;
    private final float[] nodeDistanceArray;
    private final BitArray nodeClosedArray;
    private final float[] nodeSpatialDistanceArray;
    private final float[] nodeSpatialDistancePrototype;
    private final NodeDataStructure<Integer> nodeDataStructure;

    public AstarRoutingAlgorithm( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
        this.nodePredecessorArray = new int[graph.getNodeCount()];
        this.nodeDistanceArray = new float[graph.getNodeCount()];
        this.nodeSpatialDistanceArray = new float[graph.getNodeCount()];
        this.nodeSpatialDistancePrototype = new float[graph.getNodeCount()];
        this.nodeClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
        EffectiveUtils.fillArray( nodeSpatialDistancePrototype, -1 );
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException {
//        System.out.println( "ROUTING: from = " + from.keySet() + ", to = " + to.keySet() );
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
        graph.resetNodeClosedArray( nodeClosedArray );
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
        EffectiveUtils.copyArray( nodeSpatialDistancePrototype, nodeSpatialDistanceArray );
        nodeDataStructure.clear();

        for ( NodeEntry nodeEntry : from.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            nodeDistanceArray[node] = distance;
            nodeDataStructure.add( node, distance );
            nodePredecessorArray[node] = edge;
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
//                System.out.println( "finishing - " + finalDistance + " < " + distance );
                break;
            }
            if ( to.containsKey( node ) ) { // found one of the final nodes
                if ( graph.isValidWay( node, to.get( node ).getEdgeId(), nodePredecessorArray ) ) { // is able to turn there
//                System.out.println( "found end node: " + node );
                    double nodeDistance = distance + to.get( node ).getDistance();
                    if ( nodeDistance < finalDistance ) {
//                    System.out.println( nodeDistance + " < " + finalDistance );
                        finalNode = node;
                        finalDistance = nodeDistance;
                    }
                }
            }
//            System.out.println( "outgoing array: " + Arrays.toString( graph.getOutgoingEdges( node ) ) );
            TIntIterator it = graph.getOutgoingEdgesIterator( node );
            while ( it.hasNext() ) {
                int edge = it.next();
                if ( !graph.isValidWay( node, edge, nodePredecessorArray ) ) {
                    continue;
                }
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
//                        System.out.println( "calculated distance for #" + target + " = " + calculateSpatialDistance( target, to ) );
//                        System.out.println( "inserting #" + target + " with distance = " + alternativeDistance + " + " + calculateSpatialDistance( target, to ) + " = " + ( alternativeDistance + calculateSpatialDistance( target, to ) ) );
                        nodeDataStructure.notifyDataChange( target, alternativeDistance + calculateSpatialDistance( target, to ) );
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
                NodeEntry nodeEntry = from.get( node );
                if ( nodeEntry != null && nodeEntry.getNodeId() == node && nodeEntry.getEdgeId() == pred ) { // omit the first edge
                    break;
                }
            }
        } else {
            throw new RouteNotFoundException();
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }
        return routeBuilder.build();
    }

    private float calculateSpatialDistance( int node, Map<Integer, NodeEntry> target ) {
//        System.out.println( "calculating distance for: #" + node );
        if ( nodeSpatialDistanceArray[node] < 0 ) {
            float aLat = graph.getLatitude( node );
            float aLon = graph.getLongitude( node );
//            System.out.println( "#" + node + " - coordinates[" + aLat + "," + aLon + "]" );
            float min = Float.MAX_VALUE;
            for ( NodeEntry nodeEntry : target.values() ) {
                int n = nodeEntry.getNodeId();
                float bLat = graph.getLatitude( n );
                float bLon = graph.getLongitude( n );
                float dist = (float) distanceType.calculateApproximateDistance( aLat, aLon, bLat, bLon );
//                System.out.println( "#" + entry.getKey() + " - coordinates[" + bLat + "," + bLon + "]" );
//                System.out.println( "distance to #" + entry.getKey() + " = " + dist );
                if ( dist < min ) {
                    min = dist;
                }
            }
            nodeSpatialDistanceArray[node] = min;
        }
        return nodeSpatialDistanceArray[node];
    }
}
