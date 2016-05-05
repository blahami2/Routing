/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.astar;

import cz.certicon.routing.application.algorithm.*;
import cz.certicon.routing.application.algorithm.algorithms.AbstractRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.entity.*;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A* implementation of the routing algorithm, based on the node-flight-distance
 * heuristic function.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class StraightLineAStarRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private NodeDataStructure<Node> nodeDataStructure;
    private final Map<Node.Id, Distance> distanceMap;

    public StraightLineAStarRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
        this.distanceMap = new HashMap<>();
    }

    public void setNodeDataStructure( NodeDataStructure nodeDataStructure ) {
        this.nodeDataStructure = nodeDataStructure;
    }

    public Path route( Map<Node.Id, Distance> from, Map<Node.Id, Distance> to ) {
        // clear the data structure
        nodeDataStructure.clear();
        Set<Node> closed = new HashSet<>();
//        Node nodeEqToFrom = from;
//        Node nodeEqToTo = to;
        // foreach node in G

        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            Node n = getGraph().getNode( entry.getKey() );
            n.setDistance( entry.getValue() );
            n.setPredecessorEdge( null );
            nodeDataStructure.add( n, calculateDistance( getDistanceFactory(), n, to ).getEvaluableValue() );
        }

        distanceMap.clear();
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            closed.add( currentNode );
            if ( to.containsKey( currentNode.getId() ) ) {
                // build path from predecessors and return
                return GraphUtils.createPath( getGraph(), getEntityAbstractFactory(), currentNode );
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getOutgoingEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                if ( closed.contains( endNode ) ) {
                    continue;
                }
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance;
                if ( to.containsKey( currentNode.getId() ) ) {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode, to.get( currentNode.getId() ) );
                } else {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
                }
                // replace is lower than actual

                if ( !nodeDataStructure.contains( endNode ) || tmpNodeDistance.isLowerThan( endNode.getDistance() ) ) {
//                    System.out.println( "is lower" );
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    Distance calculatedDistance = calculateDistance( getDistanceFactory(), endNode, to );
                    if ( !nodeDataStructure.contains( endNode ) ) {
                        nodeDataStructure.add( endNode, calculatedDistance.getEvaluableValue() );
                    } else {
                        nodeDataStructure.notifyDataChange( endNode, calculatedDistance.getEvaluableValue() );
                    }
                }
            }
        }
        return null;
    }

    private Distance calculateDistance( DistanceFactory distanceFactory, Node node, Map<Node.Id, Distance> target ) {
        Distance dist = distanceMap.get( node.getId() );
        if ( dist == null ) {
            Distance min = distanceFactory.createInfiniteDistance();
            for ( Map.Entry<Node.Id, Distance> entry : target.entrySet() ) {
                dist = distanceFactory.createApproximateFromCoordinates( node.getCoordinates(), getGraph().getNode( entry.getKey() ).getCoordinates() );
                if ( min.isGreaterThan( dist ) ) {
                    min = dist;
                }
            }
            dist = min;
            distanceMap.put( node.getId(), dist );
        }
        return node.getDistance().add( dist );
    }

}
