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
import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet.NodeEntry;
import cz.certicon.routing.model.collections.array.BitArray;
import cz.certicon.routing.model.collections.array.LongBitArray;
import cz.certicon.routing.utils.measuring.StatsLogger;
import cz.certicon.routing.utils.measuring.TimeLogger;
import gnu.trove.iterator.TIntIterator;
import java.util.Map;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import static cz.certicon.routing.application.RoutingAlgorithm.Utils.*;
import cz.certicon.routing.model.entity.NodeSet;

/**
 * A* implementation of the routing algorithm, based on the flight-distance
 * heuristic function.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class AstarRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private DistanceType distanceType;
    private final int[] nodePredecessorArray;
    private final float[] nodeDistanceArray;
    private final BitArray nodeClosedArray;
    private final NodeDataStructure<Integer> nodeDataStructure;

    /**
     * Constructor for {@link AstarRoutingAlgorithm}
     *
     * @param graph source of graph topology and all the necessary data
     * @param distanceType metric to calculate by
     */
    public AstarRoutingAlgorithm( Graph graph, DistanceType distanceType ) {
        super( graph );
        this.distanceType = distanceType;
        this.nodePredecessorArray = new int[graph.getNodeCount()];
        this.nodeDistanceArray = new float[graph.getNodeCount()];
        this.nodeClosedArray = new LongBitArray( graph.getNodeCount() );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
    }

    /**
     * Sets the distance type
     *
     * @param distanceType metric
     */
    public void setDistanceType( DistanceType distanceType ) {
        this.distanceType = distanceType;
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, NodeSet<Graph> nodeSet, Map<Integer, NodeSet.NodeEntry> from, Map<Integer, NodeSet.NodeEntry> to, float upperBound ) throws RouteNotFoundException {
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
        TIntFloatMap spatialDistanceMap = new TIntFloatHashMap();

        // set the source points (add to the queue)
        initArrays( graph, nodeDistanceArray, nodeDataStructure, from );
        int finalNode = -1;
        double finalDistance = upperBound;
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
//                System.out.println( "finishing - " + finalDistance + " < " + distance );
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
//                        System.out.println( "calculated distance for #" + target + " = " + calculateSpatialDistance( target, to ) );
//                        System.out.println( "inserting #" + target + " with distance = " + alternativeDistance + " + " + calculateSpatialDistance( target, to ) + " = " + ( alternativeDistance + calculateSpatialDistance( target, to ) ) );
                        nodeDataStructure.notifyDataChange( target, alternativeDistance + calculateSpatialDistance( spatialDistanceMap, target, to ) );
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
        } else if ( upperBound != Float.MAX_VALUE ) {
            updateRouteBySingleEdge( routeBuilder, nodeSet );
        } else {
            throw new RouteNotFoundException();
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
        }
        return routeBuilder.build();
    }

    /**
     * Calculates the spatial distance of the given node to the set of target
     * nodes, selects the minimum distance. Stores result in a cache (map) for
     * further computation.
     *
     * @param node the current node
     * @param target the set of target nodes
     * @return the distance
     */
    private float calculateSpatialDistance( TIntFloatMap spatialDistanceMap, int node, Map<Integer, NodeEntry> target ) {
//        System.out.println( "calculating distance for: #" + node );
        float min;
        if ( !spatialDistanceMap.containsKey( node ) ) {
            float aLat = graph.getLatitude( node );
            float aLon = graph.getLongitude( node );
//            System.out.println( "#" + node + " - coordinates[" + aLat + "," + aLon + "]" );
            min = Float.MAX_VALUE;
            for ( NodeEntry nodeEntry : target.values() ) {
                int n = getNodeId( graph, nodeEntry );
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
