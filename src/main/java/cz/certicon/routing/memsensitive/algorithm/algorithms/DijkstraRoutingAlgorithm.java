/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.algorithms;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.RouteBuilder;
import cz.certicon.routing.memsensitive.algorithm.RoutingAlgorithm;
import cz.certicon.routing.memsensitive.model.entity.Graph;
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
    private final double[] nodeDistanceArray;
    private final boolean[] nodeClosedArray;
    private final NodeDataStructure<Integer> nodeDataStructure;

    public DijkstraRoutingAlgorithm( Graph graph ) {
        this.graph = graph;
        this.nodePredecessorArray = new int[graph.getNodeCount()];
        this.nodeDistanceArray = new double[graph.getNodeCount()];
        this.nodeClosedArray = new boolean[graph.getNodeCount()];
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
    }

    @Override
    public <R> R route( RouteBuilder<R, Graph> routeBuilder, Map<Integer, Double> from, Map<Integer, Double> to ) {
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
        graph.resetNodeClosedArray( nodeClosedArray );
        nodeDataStructure.clear();

        for ( Map.Entry<Integer, Double> entry : from.entrySet() ) {
            int node = entry.getKey();
            double distance = entry.getValue();
            nodeDistanceArray[node] = distance;
            nodeDataStructure.add( node, distance );
//            System.out.println( "adding: " + node + " with distance: " + distance );
        }
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeDataStructure.isEmpty() ) {
            int node = nodeDataStructure.extractMin();
            double distance = nodeDistanceArray[node];
            nodeClosedArray[node] = true;
//            System.out.println( "Extracted: " + node + " with distance: " + distance );
            if ( finalDistance < distance ) {
                break;
            }
            if ( to.containsKey( node ) ) {
                double nodeDistance = distance + to.get( node );
                if ( nodeDistance < finalDistance ) {
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
                if ( !nodeClosedArray[target] ) {
                    double targetDistance = nodeDistanceArray[target];
                    double alternativeDistance = distance + graph.getLength( edge );
                    if ( alternativeDistance < targetDistance ) {
                        nodeDistanceArray[target] = alternativeDistance;
                        nodePredecessorArray[target] = edge;
                        if ( nodeDataStructure.contains( target ) ) {
                            nodeDataStructure.notifyDataChange( target, alternativeDistance );
//                            System.out.println( "notifying: " + target + " with distance: " + alternativeDistance );
                        } else {
                            nodeDataStructure.add( target, alternativeDistance );
//                            System.out.println( "adding: " + target + " with distance: " + alternativeDistance );
                        }
                    }
                }
            }
        }
        if ( finalNode != -1 ) {
            routeBuilder.setTargetNode( graph.getNodeOrigId( finalNode ) );
            int pred = nodePredecessorArray[finalNode];
            int currentNode = finalNode;
            while ( graph.isValidPredecessor( pred ) ) {
                routeBuilder.addEdgeAsFirst( graph, graph.getEdgeOrigId( pred ) );
                int node = graph.getOtherNode( pred, currentNode );
                pred = nodePredecessorArray[node];
                currentNode = node;
            }
        }
        return routeBuilder.build();
    }

}
