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
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.utils.CoordinateUtils;
import cz.certicon.routing.utils.EffectiveUtils;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AstarRoutingAlgorithm implements RoutingAlgorithm<Graph> {

    private final Graph graph;
    private final DistanceType distanceType;

    public AstarRoutingAlgorithm( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException {
        Map<NodeState, NodeState> nodePredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeDistanceArray = new HashMap<>();
        Set<NodeState> nodeClosedArray = new HashSet<>();
        NodeDataStructure<NodeState> nodeDataStructure = new JgraphtFibonacciDataStructure();
        TIntFloatMap spatialDistanceMap = new TIntFloatHashMap();
//        System.out.println( "ROUTING: from = " + from.keySet() + ", to = " + to.keySet() );
        routeBuilder.clear();
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }

        for ( NodeEntry nodeEntry : from.values() ) {
            int node = nodeEntry.getNodeId();
            int edge = nodeEntry.getEdgeId();
            float distance = nodeEntry.getDistance();
            NodeState state = NodeState.Factory.newInstance( node, edge );
            nodeDistanceArray.put( state, distance );
            nodeDataStructure.add( state, distance );
        }
        NodeState finalState = null;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeDataStructure.isEmpty() ) {
            NodeState state = nodeDataStructure.extractMin();
            if ( MEASURE_STATS ) {
                StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
            }
            float distance = nodeDistanceArray.get( state );
            nodeClosedArray.add( state );
//            System.out.println( "Extracted: " + node + " with distance: " + distance );
            if ( finalDistance < distance ) {
//                System.out.println( "finishing - " + finalDistance + " < " + distance );
                break;
            }
            if ( to.containsKey( state.getNode() ) ) { // found one of the final nodes
                if ( graph.isValidWay( state, to.get( state.getNode() ).getEdgeId(), nodePredecessorArray ) ) { // is able to turn there
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
//                System.out.println( "edge = " + edge + ", target = " + target );
                if ( !nodeClosedArray.contains( targetState ) && ( state.getEdge() < 0 || target != graph.getOtherNode( state.getEdge(), state.getNode() ) )) {
                    if ( !graph.isValidWay( state, edge, nodePredecessorArray ) ) {
                    } else {
                        if ( MEASURE_STATS ) {
                            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                        }
                        float targetDistance = ( nodeDistanceArray.containsKey( targetState ) ) ? nodeDistanceArray.get( targetState ) : Float.MAX_VALUE;
                        float alternativeDistance = distance + graph.getLength( edge );
                        if ( alternativeDistance < targetDistance ) {
                            nodeDistanceArray.put( targetState, alternativeDistance );
                            nodePredecessorArray.put( targetState, state );
                            nodeDataStructure.notifyDataChange( targetState, alternativeDistance + calculateSpatialDistance( spatialDistanceMap, target, to ) );
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
        if ( finalState != null ) {
//            System.out.println( "orig node as target: " + graph.getNodeOrigId( finalNode ) );
            routeBuilder.setTargetNode( graph, graph.getNodeOrigId( finalState.getNode() ) );
            NodeState currentState = finalState;
            while ( nodePredecessorArray.containsKey( currentState ) ) {// omit the first edge
                if ( !graph.isValidPredecessor( currentState.getEdge() ) ) {
                    break; // starting from crossroad
                }
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

    private float calculateSpatialDistance( TIntFloatMap spatialDistanceMap, int node, Map<Integer, NodeEntry> target ) {
//        System.out.println( "calculating distance for: #" + node );
        float min;
        if ( !spatialDistanceMap.containsKey( node ) ) {
            float aLat = graph.getLatitude( node );
            float aLon = graph.getLongitude( node );
//            System.out.println( "#" + node + " - coordinates[" + aLat + "," + aLon + "]" );
            min = Float.MAX_VALUE;
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
            spatialDistanceMap.put( node, min );
        } else {
            min = spatialDistanceMap.get( node );
        }
        return min;
    }
}
