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
import java.util.Map;

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

    @Override
    public Path route( Coordinates from, Coordinates to ) {
        // clear the data structure
        nodeDataStructure.clear();
//        Node nodeEqToFrom = from;
//        Node nodeEqToTo = to;
        // foreach node in G
        for ( Node node : getGraph().getNodes() ) {
            if ( node.getCoordinates().equals( from ) ) {
                node.setDistance( getDistanceFactory().createZeroDistance() );
                nodeDataStructure.add( node, 0 );
            } else { // set distance to infinity
                node.setDistance( getDistanceFactory().createInfiniteDistance() );
            }
        }
        Map<Coordinates, Distance> targetNodeMap = new HashMap<>();
        targetNodeMap.put( to, getDistanceFactory().createZeroDistance() );
        return route( targetNodeMap );
    }

    @Override
    public Path route( Map<Coordinates, Distance> from, Map<Coordinates, Distance> to ) {
        // clear the data structure
        nodeDataStructure.clear();
//        Node nodeEqToFrom = from;
//        Node nodeEqToTo = to;
        // foreach node in G
        for ( Node node : getGraph().getNodes() ) {
            if ( from.containsKey( node.getCoordinates() ) ) {
                Distance nodeDistance = from.get( node.getCoordinates() );
                node.setDistance( nodeDistance );
                nodeDataStructure.add( node, nodeDistance.getEvaluableValue() );
            } else { // set distance to infinity
                node.setDistance( getDistanceFactory().createInfiniteDistance() );
            }
        }
        return route( to );
    }

    private Path route( Map<Coordinates, Distance> to ) {
        distanceMap.clear();
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            if ( to.containsKey( currentNode.getCoordinates() ) ) {
                // build path from predecessors and return
                return GraphUtils.createPath( getGraph(), getEntityAbstractFactory(), currentNode );
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getOutgoingEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance;
                if ( to.containsKey( currentNode.getCoordinates() ) ) {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode, to.get( currentNode.getCoordinates() ) );
                } else {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
                }
                // replace is lower than actual
                if ( tmpNodeDistance.isLowerThan( endNode.getDistance() ) ) {
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    nodeDataStructure.notifyDataChange( endNode, calculateDistance( getDistanceFactory(), endNode, to ).getEvaluableValue() );
                }
            }
        }
        return null;
    }

    private Distance calculateDistance( DistanceFactory distanceFactory, Node node, Map<Coordinates, Distance> target ) {
        Distance dist = distanceMap.get( node.getId() );
        if ( dist == null ) {
            Distance min = distanceFactory.createInfiniteDistance();
            for ( Map.Entry<Coordinates, Distance> entry : target.entrySet() ) {
                dist = distanceFactory.createApproximateFromCoordinates( node.getCoordinates(), entry.getKey() );
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
