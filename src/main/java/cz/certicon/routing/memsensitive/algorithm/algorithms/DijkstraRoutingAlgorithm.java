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
import cz.certicon.routing.model.entity.Node;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm implements RoutingAlgorithm {

    private final Graph graph;
    private final int[] nodePredecessorArray;
    private final double[] nodeDistanceArray;
    private final NodeDataStructure<Integer> nodeDataStructure;

    public DijkstraRoutingAlgorithm( Graph graph ) {
        this.graph = graph;
        this.nodePredecessorArray = new int[graph.getNodeCount()];
        this.nodeDistanceArray = new double[graph.getNodeCount()];
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
    }

    @Override
    public <T> T route( RouteBuilder<T> routeBuilder, Map<Integer, Double> from, Map<Integer, Double> to ) {
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
        nodeDataStructure.clear();

        for ( Map.Entry<Integer, Double> entry : from.entrySet() ) {
            int node = entry.getKey();
            double distance = entry.getValue();
            nodeDistanceArray[node] = distance;
            nodeDataStructure.add( node, distance );
        }
        double min = Double.MAX_VALUE;
        for ( Double value : to.values() ) {
            min = Math.min( min, value );
        }
        int finalNode = -1;
        double finalDistance = Double.MAX_VALUE;
        while ( !nodeDataStructure.isEmpty() ) {
            int node = nodeDataStructure.extractMin();
            double distance = nodeDistanceArray[node];
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
            for ( int edge : graph.getOutgoingEdges( node ) ) {
                int target = graph.getOtherNode( edge, node );
                double targetDistance = nodeDistanceArray[target];
                double alternativeDistance = distance + graph.getLength( edge );
                if ( alternativeDistance < targetDistance ) {
                    nodeDistanceArray[target] = alternativeDistance;
                    nodePredecessorArray[target] = edge;
                    if ( nodeDataStructure.contains( node ) ) {
                        nodeDataStructure.notifyDataChange( node, alternativeDistance );
                    } else {
                        nodeDataStructure.add( node, alternativeDistance );
                    }
                }
            }
        }
        if ( finalNode != -1 ) { // build path

        }
        return routeBuilder.build();
    }

}
