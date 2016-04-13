/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.dijkstra;

import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.application.algorithm.algorithms.AbstractRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic routing algorithm implementation using the optimal Dijkstra.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private NodeDataStructure<Node> nodeDataStructure;
    private EndCondition endCondition;

    public DijkstraRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
        this.endCondition = new EndCondition() {

            @Override
            public boolean isFinished( Graph graph, Map<Coordinates, Distance> targetSet, Node currentNode ) {
                return targetSet.containsKey( currentNode.getCoordinates() );
            }

            @Override
            public Path getResult( Graph graph, GraphEntityFactory graphEntityFactory, Node targetNode ) {
                return GraphUtils.createPath( graph, graphEntityFactory, targetNode );
            }
        };
//        System.out.println( "============ DIJKSTRA =============" );
//        for ( Node node : graph.getNodes() ) {
//            System.out.println( "node: " + node.getLabel() );
//        }
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( "edge: " + edge.getLabel() );
//        }
    }

    public void setEndCondition( EndCondition endCondition ) {
        this.endCondition = endCondition;
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
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            if ( endCondition.isFinished( getGraph(), to, currentNode ) ) {
                // build path from predecessors and return
                return endCondition.getResult( getGraph(), getEntityAbstractFactory(), currentNode );
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
//                    System.out.println( "is lower" );
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                }
            }
        }
        return null;
    }

}
