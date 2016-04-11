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
    private Map<Node.Id, Distance> distanceMap;

    public StraightLineAStarRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
        this.distanceMap = new HashMap<>();
    }

    public void setNodeDataStructure( NodeDataStructure nodeDataStructure ) {
        this.nodeDataStructure = nodeDataStructure;
    }

    @Override
    public Path route( Node from, Node to ) {
        // clear the data structure
        nodeDataStructure.clear();
        // foreach node in G
        for ( Node node : getGraph().getNodes() ) {
            if ( node.getCoordinates().equals( from.getCoordinates() ) ) {
                node.setDistance( getDistanceFactory().createZeroDistance() );
                nodeDataStructure.add( node, 0 );
            } else { // set distance to infinity
                node.setDistance( getDistanceFactory().createInfiniteDistance() );
            }
        }
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            if ( currentNode.getCoordinates().equals( to.getCoordinates() ) ) {
                // build path from predecessors and return
                return GraphUtils.createPath( getGraph(), getEntityAbstractFactory(), from, currentNode );
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getOutgoingEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
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

    private Distance calculateDistance( DistanceFactory distanceFactory, Node node, Node target ) {
        Distance dist = distanceMap.get( node.getId() );
        if ( dist == null ) {
            dist = distanceFactory.createApproximateFromNodes( node, target );
            distanceMap.put( node.getId(), dist );
        }
        return node.getDistance().add( dist );
    }

}
